package org.tkit.onecx.quarkus.constraint.Size;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configurations for OneCX size constraint
 */
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "size")
public interface SizeConfig {
    /**
     * Template for the constraint violations error message
     * 4 parameters required: %1$s, %2$s, %3$d, %4$d
     * %1$s = prefix
     * %2$s = message
     * %3$d = min-size value
     * %4$d = max-size value
     */
    @WithName("template")
    @WithDefault("%1$s: %2$s (%3$d Bytes - %4$d Bytes)")
    String template();

    /**
     * Prefix for constraint violations error message
     */
    @WithName("prefix")
    @WithDefault("OneCX Size Constraint")
    String prefix();
}
