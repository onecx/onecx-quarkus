package org.tkit.onecx.quarkus.parameter;

import jakarta.enterprise.inject.Default;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;
import org.tkit.onecx.quarkus.parameter.metrics.ParametersMetricsService;

@Recorder
public class ParametersRecorder {

    private static final Logger log = LoggerFactory.getLogger(ParametersRecorder.class);

    public void configSources(BeanContainer container, ParametersConfig config) {
        // check if the parameter extension is enabled
        if (!config.enabled()) {
            log.debug(
                    "No attempt will be made to obtain configuration from Parameters management because the functionality has been disabled via configuration");
            return;
        }

        // init parameter service
        ParametersService service = container.beanInstance(ParametersService.class, Default.Literal.INSTANCE);
        service.init(config);

        // if metrics service enabled
        if (config.metrics().enabled()) {
            // init metrics service
            ParametersMetricsService metrics = container.beanInstance(ParametersMetricsService.class, Default.Literal.INSTANCE);
            metrics.init(config);
        }
    }

}
