package org.tkit.onecx.quarkus.validator.config;

import java.util.Map;

import org.tkit.onecx.quarkus.validator.size.SizeValidatorConfig;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configurations for OneCX Validator
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.validator")
public interface ValidatorConfig {

    /**
     * Parameter configuration for validator extension
     */
    @WithName("values")
    ValidatorsValuesConfig values();

    /**
     * Configuration for size constraint
     */
    @WithName("size")
    SizeValidatorConfig size();

    /**
     * Parameter configuration for validator extension
     */
    interface ValidatorsValuesConfig {

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
