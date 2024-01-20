package io.github.onecx.quarkus.security;

import java.util.List;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(prefix = "onecx", name = "permissions", phase = ConfigPhase.BUILD_TIME)
public class SecurityBuildTimeConfig {

    /**
     * Mapping annotation configuration.
     */
    @ConfigItem(name = "mapping-annotation")
    public MappingBuildTimeConfig mapping = new MappingBuildTimeConfig();

    /**
     * Mapping @PermissionsAllowed from interface to implementation class
     */
    @ConfigGroup
    public static class MappingBuildTimeConfig {

        /**
         * Enable interface mapping
         */
        @ConfigItem(name = "enabled", defaultValue = "true")
        public boolean enabled;

        /**
         * Mapping includes packages.
         */
        @ConfigItem(name = "packages", defaultValue = "io.github.onecx,gen.io.github.onecx")
        public List<String> packages;
    }
}
