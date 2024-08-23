package org.tkit.onecx.quarkus.security;

import java.util.List;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "onecx.security")
public interface SecurityBuildTimeConfig {

    /**
     * Mapping annotation configuration.
     */
    @WithName("mapping-annotation")
    MappingBuildTimeConfig mapping();

    /**
     * Mapping @PermissionsAllowed from interface to implementation class
     */
    interface MappingBuildTimeConfig {

        /**
         * Enable interface mapping
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        /**
         * Mapping includes packages.
         */
        @WithName("packages")
        @WithDefault("org.tkit.onecx,gen.org.tkit.onecx")
        List<String> packages();
    }
}
