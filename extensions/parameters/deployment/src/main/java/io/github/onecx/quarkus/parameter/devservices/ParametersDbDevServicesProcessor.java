package io.github.onecx.quarkus.parameter.devservices;

import io.github.onecx.quarkus.parameter.ParametersConfig;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class ParametersDevServicesProcessor {

    private static final Logger log = LoggerFactory.getLogger(ParametersDevServicesProcessor.class);

    /**
     * Label to add to shared Dev Service for Parameters running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    private static final String DEV_SERVICE_LABEL = "onecx-dev-service-parameters";
    private static final int PARAMETERS_EXPOSED_PORT = 8080;

    private static final ContainerLocator containerLocator = new ContainerLocator(DEV_SERVICE_LABEL, PARAMETERS_EXPOSED_PORT);

    private static final String FEATURE_NAME = "mock-server";

    private static final String DEFAULT_IMAGE = "localhost/onecx-parameter-svc:999-SNAPSHOT";

    private static volatile DevServicesResultBuildItem.RunningDevService devServices;
    private static volatile ParametersBuildTimeConfig.DevServiceConfiguration capturedDevServicesConfiguration;
    private static volatile boolean first = true;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startParametersContainers(LaunchModeBuildItem launchMode,
                                                                DockerStatusBuildItem dockerStatusBuildItem,
                                                                List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
                                                                ParametersBuildTimeConfig config,
                                                                Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
                                                                CuratedApplicationShutdownBuildItem closeBuildItem,
                                                                LoggingSetupBuildItem loggingSetupBuildItem,
                                                                GlobalDevServicesConfig devServicesConfig) {
        var currentDevServicesConfiguration = config.defaultDevService;

        // figure out if we need to shut down and restart existing Parameters containers
        // if not and the Parameters containers have already started we just return
        if (devServices != null) {
            boolean restartRequired = !currentDevServicesConfiguration.equals(capturedDevServicesConfiguration);
            if (!restartRequired) {
                return devServices.toBuildItem();
            }
            try {
                devServices.close();
            } catch (Throwable e) {
                log.error("Failed to stop Parameters container", e);
            }
            devServices = null;
            capturedDevServicesConfiguration = null;
        }

        capturedDevServicesConfiguration = currentDevServicesConfiguration;

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Parameters Dev Services Starting:", consoleInstalledBuildItem,
                loggingSetupBuildItem);
        try {

            devServices = startContainer(dockerStatusBuildItem,
                    currentDevServicesConfiguration.devservices,
                    launchMode.getLaunchMode(),
                    !devServicesSharedNetworkBuildItem.isEmpty(), devServicesConfig.timeout);
            if (devServices == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }

        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devServices == null) {
            return null;
        }

        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devServices != null) {
                    try {
                        devServices.close();
                    } catch (Throwable t) {
                        log.error("Failed to stop MockServer", t);
                    }
                }
                first = true;
                devServices = null;
                capturedDevServicesConfiguration = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        if (devServices.isOwner()) {
            log.info("The mock-server server is ready to accept connections on {}",
                    devServices.getConfig().get(ParametersConfig.HOST));
        }
        return devServices.toBuildItem();
    }

    private DevServicesResultBuildItem.RunningDevService startContainer(DockerStatusBuildItem dockerStatusBuildItem,
                                                                        DevServicesConfig devServicesConfig, LaunchMode launchMode,
                                                                        boolean useSharedNetwork, Optional<Duration> timeout) {
        if (!devServicesConfig.enabled) {
            // explicitly disabled
            log.debug("Not starting devservices for rest client as it has been disabled in the config");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            log.warn("Please configure '{}' or get a working Parameters instance", ParametersConfig.HOST);
            return null;
        }

        DockerImageName dockerImageName = DockerImageName.parse(devServicesConfig.imageName.orElse(DEFAULT_IMAGE));

        return null;
    }

    private static class ParametersContainer extends GenericContainer<ParametersContainer> {

        private final boolean useSharedNetwork;
        private final int fixedExposedPort;

        private String hostName = null;

        public ParametersContainer(DockerImageName image, int fixedExposedPort, String serviceName,
                                boolean useSharedNetwork, String importFile, UnleashDbDevServicesProviderBuildItem dbSettings) {
            super(image);
            log.debug("Parameters docker image {}", image);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;

            this.withExposedPorts(DEFAULT_UNLEASH_PORT);
    }
}
