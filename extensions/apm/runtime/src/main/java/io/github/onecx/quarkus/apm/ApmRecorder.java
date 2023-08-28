package io.github.onecx.quarkus.apm;

import jakarta.enterprise.inject.Default;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class ApmRecorder {

    private static final Logger log = LoggerFactory.getLogger(ApmRecorder.class);

    static final String UNKNOWN_SERVICE_NAME = "quarkus/unknown";

    public void init(BeanContainer container, ApplicationConfig appConfig, ApmConfig config) {
        // check if the parameter extension is enabled
        if (!config.enabled) {
            log.debug(
                    "No attempt will be made to obtain configuration from APM management because the functionality has been disabled via configuration");
            return;
        }

        // Check if there was an applicationId set
        String applicationId = config.applicationId.orElseGet(() -> appConfig.name.orElse(UNKNOWN_SERVICE_NAME));

        // init APM service
        PermissionClientService service = container.instance(PermissionClientService.class, Default.Literal.INSTANCE);
        service.init(config, applicationId);
    }
}
