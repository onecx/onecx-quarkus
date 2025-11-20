package org.tkit.onecx.quarkus.parameter.runtime;

import jakarta.enterprise.inject.Default;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.history.ParametersHistoryService;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ParametersRecorder {

    private static final Logger log = LoggerFactory.getLogger(ParametersRecorder.class);

    private final RuntimeValue<ParametersConfig> configValue;

    public ParametersRecorder(RuntimeValue<ParametersConfig> configValue) {
        this.configValue = configValue;
    }

    public void configSources(BeanContainer container) {
        ParametersConfig config = configValue.getValue();
        // check if the parameter extension is enabled
        if (!config.enabled()) {
            log.debug(
                    "No attempt will be made to obtain configuration from Parameters management because the functionality has been disabled via configuration");
            return;
        }

        // init parameter service
        ParametersDataService service = container.beanInstance(ParametersDataService.class, Default.Literal.INSTANCE);
        service.init(config);

        // if metrics service enabled
        if (config.history().enabled()) {
            // init metrics service
            ParametersHistoryService metrics = container.beanInstance(ParametersHistoryService.class, Default.Literal.INSTANCE);
            metrics.init(config);
        }
    }

}
