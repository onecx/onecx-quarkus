package io.github.onecx.quarkus.it.parameter;

import jakarta.enterprise.inject.Default;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ParametersRecorder {

    static final String UNKNOWN_SERVICE_NAME = "quarkus/unknown";

    private static final Logger log = LoggerFactory.getLogger(ParametersRecorder.class);

    public void configSources(BeanContainer container, ApplicationConfig appConfig, ParametersConfig config) {
        // check if the parameter extension is enabled
        if (!config.enabled) {
            log.debug(
                    "No attempt will be made to obtain configuration from Parameters management because the functionality has been disabled via configuration");
            return;
        }

        // Check if there was an applicationId set
        String applicationId = config.applicationId.orElseGet(() -> appConfig.name.orElse(UNKNOWN_SERVICE_NAME));

        // init parameter service
        ParametersService service = container.instance(ParametersService.class, Default.Literal.INSTANCE);
        service.init(config, applicationId);

        // if metrics service enabled
        if (config.metrics.enabled) {
            // init metrics service
            ParametersMetricsService metrics = container.instance(ParametersMetricsService.class, Default.Literal.INSTANCE);
            metrics.init(config, applicationId);
        }
    }

}
