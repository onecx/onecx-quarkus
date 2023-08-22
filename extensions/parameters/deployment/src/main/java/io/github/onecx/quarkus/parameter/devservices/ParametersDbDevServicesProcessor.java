package io.github.onecx.quarkus.parameter.devservices;

import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.github.onecx.quarkus.parameter.ParametersBuildTimeConfig;
import io.github.onecx.quarkus.parameter.ParametersConfig;
import io.github.onecx.quarkus.parameter.ParametersProcessor;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;

public class ParametersDbDevServicesProcessor {

    private static final Logger log = LoggerFactory.getLogger(ParametersDbDevServicesProcessor.class);

    static volatile ParametersDbRunningDevService devService;

    static volatile ParametersDbDevServiceCfg cfg;

    static volatile boolean first = true;

    public static final String DB_FEATURE_NAME = ParametersProcessor.FEATURE + "-db";
    private static final String DB_ALIAS = DB_FEATURE_NAME;

    private static final String DEV_SERVICE_LABEL = "onecx-dev-service-parameters-db";
    public static final int DEFAULT_PORT = 5432;

    private static final ContainerLocator containerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            DEFAULT_PORT);

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startDbContainers(LaunchModeBuildItem launchMode,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            ParametersBuildTimeConfig buildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            BuildProducer<ParametersDbDevServicesProviderBuildItem> startResultProducer,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            DockerStatusBuildItem dockerStatusBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig) {

        ParametersDbDevServiceCfg configuration = getConfiguration(buildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            stopContainer();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Parameters Database Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);

        try {
            devService = startContainer(dockerStatusBuildItem, configuration, launchMode,
                    !devServicesSharedNetworkBuildItem.isEmpty(),
                    devServicesConfig.timeout);
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }
        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    stopContainer();
                }
                first = true;
                devService = null;
                cfg = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfg = configuration;

        if (devService.isOwner()) {

            log.info("The Parameters database is ready to accept connections. Config: {}",
                    devService.jdbcUrl);

            startResultProducer.produce(new ParametersDbDevServicesProviderBuildItem(
                    devService.jdbcUrl,
                    devService.dbUsername,
                    devService.dbPassword));
        }

        return devService.toBuildItem();

    }

    private ParametersDbRunningDevService startContainer(DockerStatusBuildItem dockerStatusBuildItem,
            ParametersDbDevServiceCfg config,
            LaunchModeBuildItem launchMode, boolean useSharedNetwork, Optional<Duration> timeout) {

        if (!config.devServicesEnabled) {
            // explicitly disabled
            log.debug("Not starting dev services for Parameters as it has been disabled in the config");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            log.warn(
                    "Docker isn't working, please configure the Parameters URL property ({}).", ParametersConfig.HOST);
            return null;
        }

        final Optional<ContainerAddress> maybeContainerAddress = containerLocator.locateContainer(config.serviceName,
                config.shared,
                launchMode.getLaunchMode());

        // Starting the broker
        final Supplier<ParametersDbRunningDevService> defaultSupplier = () -> {

            ParametersPostgreSQLContainer container = new ParametersPostgreSQLContainer(
                    config.imageName,
                    launchMode.getLaunchMode() == DEVELOPMENT ? config.serviceName : null,
                    useSharedNetwork);

            timeout.ifPresent(container::withStartupTimeout);

            // enable test-container reuse
            if (config.reuse) {
                container.withReuse(true);
            }

            container.start();

            return new ParametersDbRunningDevService(DB_FEATURE_NAME, container.getContainerId(),
                    new ContainerShutdownCloseable(container, DB_FEATURE_NAME),
                    container.getJdbcUrl(),
                    container.getUsername(),
                    container.getPassword());
        };

        return maybeContainerAddress
                .map(containerAddress -> new ParametersDbRunningDevService(DB_FEATURE_NAME, containerAddress.getId(), null))
                .orElseGet(defaultSupplier);
    }

    private ParametersDbDevServiceCfg getConfiguration(ParametersBuildTimeConfig cfg) {
        DevServicesConfig devServicesConfig = cfg.devServices;
        return new ParametersDbDevServiceCfg(devServicesConfig);
    }

    private static final class ParametersDbDevServiceCfg {

        private final boolean devServicesEnabled;
        private final String imageName;
        private final boolean shared;
        private final String serviceName;

        private final boolean reuse;

        public ParametersDbDevServiceCfg(DevServicesConfig config) {
            this.devServicesEnabled = config.enabled;
            DevServicesConfig.ParametersDatabaseConfig tmp = config.db;
            this.imageName = tmp.imageName;
            this.serviceName = tmp.serviceName;
            this.shared = config.shared;
            this.reuse = config.reuse;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ParametersDbDevServiceCfg that = (ParametersDbDevServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && Objects.equals(imageName, that.imageName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName);
        }
    }

    private static class ParametersPostgreSQLContainer extends PostgreSQLContainer<ParametersPostgreSQLContainer> {
        private final boolean useSharedNetwork;
        private String hostName = null;

        public ParametersPostgreSQLContainer(String imageName, String serviceName,
                boolean useSharedNetwork) {
            super(DockerImageName
                    .parse(imageName)
                    .asCompatibleSubstituteFor(DockerImageName.parse(PostgreSQLContainer.IMAGE)));

            log.debug("Parameters database docker image {}", imageName);

            this.useSharedNetwork = useSharedNetwork;
            if (serviceName != null) {
                this.withLabel(DEV_SERVICE_LABEL, serviceName);
            }

            addExposedPort(POSTGRESQL_PORT);
            hostName = DB_ALIAS;
            this.withNetworkAliases(DB_ALIAS);
        }

        @Override
        protected void configure() {
            super.configure();
            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, DB_ALIAS);
            } else {
                withNetwork(Network.SHARED);
            }
        }

        @Override
        public String getJdbcUrl() {
            String additionalUrlParams = this.constructUrlParameters("?", "&");
            return "jdbc:postgresql://" + hostName + ":" + POSTGRESQL_PORT + "/" + this.getDatabaseName()
                    + additionalUrlParams;
        }

    }

    public static class ParametersDbRunningDevService extends DevServicesResultBuildItem.RunningDevService {

        public String dbUsername;
        public String dbPassword;
        public String jdbcUrl;

        public ParametersDbRunningDevService(String name, String containerId, Closeable closeable) {
            super(name, containerId, closeable, Map.of());
        }

        public ParametersDbRunningDevService(String name, String containerId, Closeable closeable, String jdbcUrl,
                String dbUsername, String dbPassword) {
            super(name, containerId, closeable, Map.of());
            this.jdbcUrl = jdbcUrl;
            this.dbUsername = dbUsername;
            this.dbPassword = dbPassword;
        }
    }

    private void stopContainer() {

        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the Parameters database", e);
            } finally {
                devService = null;
            }
        }
    }
}
