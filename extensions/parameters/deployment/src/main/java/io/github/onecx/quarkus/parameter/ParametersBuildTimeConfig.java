package io.github.onecx.quarkus.parameter;

import io.github.onecx.quarkus.parameter.devservices.DevServicesConfig;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "parameters", prefix = "onecx")
public class ParametersBuildTimeConfig {

    /**
     * Configuration for DevServices
     * <p>
     * DevServices allows Quarkus to automatically start MockServer in dev and test mode.
     */
    @ConfigItem(name = "devservices")
    public DevServicesConfig devServices;

}
