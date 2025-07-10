package org.tkit.onecx.quarkus.validator.size;

import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configurations for OneCX size validator
 */
public interface SizeValidatorConfig {
    /**
     * Template for the constraint violations error message
     * 6 parameters required: %1$s, %2$s, %3$s, %4$s, %5$d, %6$d <br>
     * %1$s = provider <br>
     * %2$s = key <br>
     * %3$s = parameter <br>
     * %4$s = message <br>
     * %5$d = min-size value <br>
     * %6$d = max-size value
     */
    @WithName("template")
    @WithDefault("Parameter: %3$s  Boundaries: %5$d Bytes - %6$d Bytes")
    String template();

}
