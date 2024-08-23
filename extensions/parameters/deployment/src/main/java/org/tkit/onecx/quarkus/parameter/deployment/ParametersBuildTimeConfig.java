package org.tkit.onecx.quarkus.parameter.deployment;

import org.tkit.onecx.quarkus.parameter.deployment.devservices.DevServicesConfig;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "onecx.parameters")
public interface ParametersBuildTimeConfig {

    /**
     * Configuration for DevServices
     * <p>
     * DevServices allows Quarkus to automatically start MockServer in dev and test mode.
     */
    @WithName("devservices")
    DevServicesConfig devServices();

}
