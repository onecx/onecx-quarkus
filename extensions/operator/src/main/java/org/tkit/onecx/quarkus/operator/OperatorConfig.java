package org.tkit.onecx.quarkus.operator;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Operator extension configuration
 */
@ConfigMapping(prefix = "onecx.operator")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface OperatorConfig {

    /**
     * Annotation name to set if resource was touched
     */
    @WithName(value = "touch-annotation")
    @WithDefault(value = "org.tkit.onecx.touchedAt")
    String touchAnnotation();
}