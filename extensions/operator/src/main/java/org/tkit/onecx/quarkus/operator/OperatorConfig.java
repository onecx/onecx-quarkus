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

    String TOUCH_ANNOTATION_DEFAULT = "org.tkit.onecx.touchedAt";

    /**
     * Operator touch configuration
     */
    @WithName(value = "touched")
    TouchedConfig touched();

    /**
     * Operator touch configuration
     */
    interface TouchedConfig {

        /**
         * Annotation name to set if resource was touched at
         */
        @WithName(value = "annotation-at")
        @WithDefault(value = TOUCH_ANNOTATION_DEFAULT)
        String annotationAt();

        /**
         * Annotation name to set if resource was touched by
         */
        @WithName(value = "annotation-by")
        @WithDefault(value = "org.tkit.onecx.touchedBy")
        String annotationBy();

    }
}