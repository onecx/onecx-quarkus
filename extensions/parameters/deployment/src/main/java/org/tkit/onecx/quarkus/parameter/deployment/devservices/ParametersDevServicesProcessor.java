package org.tkit.onecx.quarkus.parameter.deployment.devservices;

import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.tkit.onecx.quarkus.parameter.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.deployment.ParametersBuildTimeConfig;
import org.tkit.onecx.quarkus.parameter.deployment.ParametersProcessor;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;

public class ParametersDevServicesProcessor {

    private static final Logger log = LoggerFactory.getLogger(ParametersDevServicesProcessor.class);

    private static final String DEFAULT_DOCKER_IMAGE = "ghcr.io/onecx/onecx-parameter-svc:main";
    private static final String DEV_SERVICE_LABEL = "onecx-dev-service-parameters";
    private static final String IMPORT_FILE_PATH = "/tmp/parameters-import-file.json";
    public static final int DEFAULT_PORT = 8080;
    private static final ContainerLocator containerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            DEFAULT_PORT);
    static volatile ParametersRunningDevService devService;
    static volatile ParametersDevServiceCfg cfg;
    static volatile boolean first = true;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startContainers(LaunchModeBuildItem launchMode,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            ParametersBuildTimeConfig buildTimeConfig,
            ParametersDbDevServicesProviderBuildItem dbSettings,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            DockerStatusBuildItem dockerStatusBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig) {

        ParametersDevServiceCfg configuration = getConfiguration(buildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            stopContainer();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Parameters Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startContainer(dockerStatusBuildItem, configuration, dbSettings, launchMode,
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
            String tmp = devService.getConfig().get(ParametersConfig.HOST);
            log.info("The parameters is ready to accept connections on {}", tmp);
        }

        return devService.toBuildItem();

    }

    private ParametersRunningDevService startContainer(DockerStatusBuildItem dockerStatusBuildItem,
            ParametersDevServiceCfg config, ParametersDbDevServicesProviderBuildItem dbSettings,
            LaunchModeBuildItem launchMode, boolean useSharedNetwork, Optional<Duration> timeout) {
        if (!config.devServicesEnabled) {
            // explicitly disabled
            log.debug("Not starting dev services for Parameters as it has been disabled in the config");
            return null;
        }

        if (ConfigUtils.isPropertyPresent(ParametersConfig.HOST)) {
            log.debug("Not starting dev services for Parameters as '{}' have been provided", ParametersConfig.HOST);
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

        // Starting to parameters service
        final Supplier<ParametersRunningDevService> defaultParametersSupplier = () -> {

            DockerImageName image = DockerImageName.parse(config.imageName);

            ParametersContainer container = new ParametersContainer(
                    image,
                    config.fixedExposedPort,
                    launchMode.getLaunchMode() == DEVELOPMENT ? config.serviceName : null,
                    useSharedNetwork,
                    config.importFile,
                    dbSettings);
            timeout.ifPresent(container::withStartupTimeout);

            if (config.log) {
                container.withLogConsumer(ContainerLogger.create(config.serviceName));
            }

            // enable test-container reuse
            if (config.reuse) {
                container.withReuse(true);
            }

            container.start();

            return new ParametersRunningDevService(ParametersProcessor.FEATURE, container.getContainerId(),
                    new ContainerShutdownCloseable(container, ParametersProcessor.FEATURE),
                    configMap(container.getUrl()));
        };

        return maybeContainerAddress
                .map(containerAddress -> new ParametersRunningDevService(ParametersProcessor.FEATURE,
                        containerAddress.getId(),
                        null, configMap(containerAddress.getUrl())))
                .orElseGet(defaultParametersSupplier);
    }

    private static Map<String, String> configMap(String url) {
        Map<String, String> config = new HashMap<>();
        config.put(ParametersConfig.HOST, url);
        return config;
    }

    public static class ParametersRunningDevService extends DevServicesResultBuildItem.RunningDevService {

        public ParametersRunningDevService(String name, String containerId, Closeable closeable, Map<String, String> config) {
            super(name, containerId, closeable, config);
        }
    }

    private ParametersDevServiceCfg getConfiguration(ParametersBuildTimeConfig cfg) {
        var devServicesConfig = cfg.devServices;
        return new ParametersDevServiceCfg(devServicesConfig);
    }

    private static final class ParametersDevServiceCfg {

        private final boolean devServicesEnabled;
        private final String imageName;
        private final Integer fixedExposedPort;
        private final boolean shared;
        private final String serviceName;

        private final String importFile;

        private final boolean reuse;

        private final boolean log;

        public ParametersDevServiceCfg(DevServicesConfig config) {
            this.devServicesEnabled = config.enabled;
            this.imageName = config.imageName.orElse(DEFAULT_DOCKER_IMAGE);
            this.fixedExposedPort = config.port.orElse(0);
            this.shared = config.shared;
            this.serviceName = config.serviceName;
            this.reuse = config.reuse;
            this.importFile = config.importFile.orElse(null);
            this.log = config.log;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ParametersDevServiceCfg that = (ParametersDevServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && Objects.equals(imageName, that.imageName)
                    && Objects.equals(fixedExposedPort, that.fixedExposedPort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName, fixedExposedPort);
        }
    }

    private void stopContainer() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the Parameters service", e);
            } finally {
                devService = null;
            }
        }
    }

    private static class ParametersContainer extends GenericContainer<ParametersContainer> {

        private final boolean useSharedNetwork;
        private final int fixedExposedPort;

        private String hostName = null;

        public ParametersContainer(DockerImageName image, int fixedExposedPort, String serviceName,
                boolean useSharedNetwork, String importFile, ParametersDbDevServicesProviderBuildItem dbSettings) {
            super(image);
            log.debug("Parameters docker image {}", image);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;

            this.withExposedPorts(DEFAULT_PORT);

            this.withEnv("_PROD_TKIT_LOG_JSON_ENABLED", "false");

            // set up the database for parameters service
            this.withEnv("_PROD_QUARKUS_DATASOURCE_JDBC_URL", dbSettings.jdbcUrl)
                    .withEnv("_PROD_QUARKUS_DATASOURCE_USERNAME", dbSettings.username)
                    .withEnv("_PROD_QUARKUS_DATASOURCE_PASSWORD", dbSettings.password);

            if (serviceName != null) {
                this.withLabel(DEV_SERVICE_LABEL, serviceName);
            }

            // import data file
            if (importFile != null) {

                this.withEnv("TKIT_DATAIMPORT_ENABLED", "true");
                this.withEnv("TKIT_DATAIMPORT_CONFIGURATIONS_PARAMETERS_ENABLED", "true");
                this.withEnv("TKIT_DATAIMPORT_CONFIGURATIONS_PARAMETERS_FILE", IMPORT_FILE_PATH);
                this.withEnv("TKIT_DATAIMPORT_CONFIGURATIONS_PARAMETERS_METADATA_OPERATION", "CLEAN_INSERT");

                if (Files.isRegularFile(Path.of(importFile))) {
                    log.info("Parameters startup import regular file: {}", importFile);
                    this.withFileSystemBind(importFile, IMPORT_FILE_PATH, BindMode.READ_ONLY);
                } else {
                    log.info("Parameters startup import class-path file: {}", importFile);
                    this.withClasspathResourceMapping(importFile, IMPORT_FILE_PATH, BindMode.READ_ONLY);
                }
            }

            // wait for start
            this.waitingFor(Wait.forHttp("/q/health"));
        }

        public String getExternalAddress(final int port) {
            return this.getHost() + ":" + this.getMappedPort(port);
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "parameters");
                return;
            } else {
                withNetwork(Network.SHARED);
            }

            if (fixedExposedPort > 0) {
                addFixedExposedPort(fixedExposedPort, DEFAULT_PORT);
            } else {
                addExposedPort(DEFAULT_PORT);
            }
        }

        public int getPort() {
            if (useSharedNetwork) {
                return DEFAULT_PORT;
            }
            if (fixedExposedPort > 0) {
                return fixedExposedPort;
            }
            return super.getFirstMappedPort();
        }

        /**
         * Returns an address accessible from within the container's network for the given port.
         *
         * @param port the target port
         * @return internally accessible address for {@code port}
         */
        public String getInternalAddress(final int port) {
            return getInternalHost() + ":" + port;
        }

        /**
         * Returns a hostname which is accessible from a host that is within the same docker network as
         * this node. It will attempt to return the last added network alias it finds, and if there is
         * none, will return the container name. The network alias is preferable as it typically conveys
         * more meaning than container name, which is often randomly generated.
         *
         * @return the hostname of this node as visible from a host within the same docker network
         */
        @API(status = API.Status.EXPERIMENTAL)
        public String getInternalHost() {
            final GenericContainer<?> container = self();
            final List<String> aliases = container.getNetworkAliases();
            if (aliases.isEmpty()) {
                return container.getContainerInfo().getName();
            }

            return aliases.get(aliases.size() - 1);
        }

        public String getParametersHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }

        public String getUrl() {
            return String.format("http://%s:%d/", this.getParametersHost(), this.getPort());
        }
    }
}
