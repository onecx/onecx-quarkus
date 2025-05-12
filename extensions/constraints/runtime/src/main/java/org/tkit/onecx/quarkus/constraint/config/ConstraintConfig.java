package org.tkit.onecx.quarkus.constraint.config;

import java.util.Map;

import org.tkit.onecx.quarkus.constraint.Size.SizeConfig;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configurations for OneCX Constraints
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.constraints")
public interface ConstraintConfig {

    /**
     * Parameter configuration for validator extension
     */
    @WithName("parameters")
    ValidatorsParameterConfig parameters();

    /**
     * Configuration for size constraint
     */
    @WithName("size")
    SizeConfig size();

    interface ValidatorsParameterConfig {

        /**
         * Whether parameter-svc is enabled in case the parameter extension is present.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        /**
         * Key to parameter-name mapping
         */
        @WithName("mapping")
        Map<String, String> mapping();
    }
}
