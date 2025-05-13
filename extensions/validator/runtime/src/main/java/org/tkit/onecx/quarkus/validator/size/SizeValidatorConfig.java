package org.tkit.onecx.quarkus.validator.size;

import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configurations for OneCX size validator
 */
public interface SizeValidatorConfig {
    /**
     * Template for the constraint violations error message
     * 4 parameters required: %1$s, %2$s, %3$s, %4$d, %5$d
     * %1$s = provider
     * %2$s = key
     * %3$s = parameter
     * %4$s = message
     * %5$d = min-size value
     * %6$d = max-size value
     */
    @WithName("template")
    @WithDefault("OneCX Size constraint. Provider: %1$s Key: %2$s Parameter: %3$s Message: %4$s (%5$d Bytes - %6$d Bytes)")
    String template();

}
