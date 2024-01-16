package io.github.onecx.quarkus.permission;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.HttpHeaders;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(prefix = "onecx", name = "permissions", phase = ConfigPhase.RUN_TIME)
public class PermissionRuntimeConfig {

    /**
     * Enable interface mapping
     */
    @ConfigItem(name = "enabled", defaultValue = "true")
    public boolean enabled;

    /**
     * Allow all permissions
     */
    @ConfigItem(name = "allow-all", defaultValue = "false")
    public boolean allowAll;

    /**
     * Permissions application ID.
     */
    @ConfigItem(name = "application-id", defaultValue = "${quarkus.application.name}")
    public String applicationId;

    /**
     * Permissions prefix name.
     */
    @ConfigItem(name = "name", defaultValue = "onecx")
    public String name;

    /**
     * Permissions token header parameter.
     */
    @ConfigItem(name = "request-token-from-header-param", defaultValue = HttpHeaders.AUTHORIZATION)
    public String requestTokenHeaderParam;

    /**
     * Permissions resource action separator.
     */
    @ConfigItem(name = "key-separator", defaultValue = "#")
    public String keySeparator;

    /**
     * Permissions APM token header parameter
     */
    @ConfigItem(name = "token-header-param", defaultValue = "apm-principal-token")
    public String tokenHeaderParam;

    /**
     * Mock configuration
     */
    @ConfigItem(name = "mock")
    public MockConfig mock;

    @ConfigGroup
    public static class MockConfig {

        /**
         * Enable or disable mock service
         */
        @ConfigItem(name = "enabled", defaultValue = "false")
        boolean enabled;

        /**
         * Mock data for role
         * Map format : <role>.<permissions>
         */
        @ConfigItem(name = "roles")
        Map<String, Map<String, List<String>>> roles;
    }

}
