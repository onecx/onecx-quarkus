package org.tkit.onecx.quarkus.parameter.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
@ConfigMapping(prefix = "onecx.parameters.build")
public interface ParametersBuildTimeConfig {

    /**
     * Metrics configuration.
     */
    @WithName("metrics")
    MetricsConfig metrics();

    /**
     * Metrics configuration.
     */
    interface MetricsConfig {
        /**
         * Whether a metrics is enabled in case the micrometer or micro-profile metrics extension is present.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();
    }
}
