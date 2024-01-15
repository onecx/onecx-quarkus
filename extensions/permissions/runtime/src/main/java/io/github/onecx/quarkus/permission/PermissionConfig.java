package io.github.onecx.quarkus.permission;

import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.core.HttpHeaders;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "onecx.permissions")
public interface PermissionConfig {

    @WithName(value = "application-id")
    @WithDefault(value = "${quarkus.application.name}")
    String applicationId();

    @WithName(value = "enabled")
    @WithDefault(value = "true")
    boolean enabled();

    @WithName(value = "name")
    @WithDefault(value = "onecx")
    String name();

    @WithName(value = "permissions-token-from-header-param")
    @WithDefault(value = HttpHeaders.AUTHORIZATION)
    String permissionTokenHeaderParam();

    @WithName(value = "key-separator")
    @WithDefault(value = "#")
    String keySeparator();

    /**
     * Permission token header id
     */
    @WithName(value = "token-header-param")
    @WithDefault(value = "apm-principal-token")
    String tokenHeaderParam();

    @WithName("mock")
    MockConfig mock();

    interface MockConfig {

        /**
         * Enable or disable tenant mock service.
         */
        @WithName("enabled")
        @WithDefault("false")
        boolean enabled();

        /**
         * Mock data
         * Map format : <role>.<permissions>
         */
        @WithName("data")
        Map<String, MockPermissionConfig> data();

    }

    interface MockPermissionConfig {

        /**
         * Mock data
         * Map format : <action>.<permissions>
         */
        Map<String, Set<String>> resources();
    }
}
