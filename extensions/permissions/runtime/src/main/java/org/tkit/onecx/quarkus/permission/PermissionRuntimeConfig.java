package org.tkit.onecx.quarkus.permission;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.HttpHeaders;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.permissions")
public interface PermissionRuntimeConfig {

    /**
     * Tenant client URL configuration.
     * This property is alias for rest-client generated configuration property `quarkus.rest-client.onecx_permission.url`
     */
    @WithName("service.client.url")
    @WithDefault("http://onecx-permission-svc:8080")
    String clientUrl();

    /**
     * Enable interface mapping
     */
    @WithName("enabled")
    @WithDefault("true")
    boolean enabled();

    /**
     * Enable interface mapping
     */
    @WithName("cache-enabled")
    @WithDefault("true")
    boolean cacheEnabled();

    /**
     * Allow all permissions
     */
    @WithName("allow-all")
    @WithDefault("false")
    boolean allowAll();

    /**
     * Product name.
     */
    @WithName("product-name")
    String productName();

    /**
     * Permissions application ID.
     */
    @WithName("application-id")
    @WithDefault("${quarkus.application.name}")
    String applicationId();

    /**
     * Permissions prefix name.
     */
    @WithName("name")
    @WithDefault("onecx")
    String name();

    /**
     * Permissions access token header parameter.
     */
    @WithName("request-token-from-header-param")
    @WithDefault(HttpHeaders.AUTHORIZATION)
    String requestTokenHeaderParam();

    /**
     * Permissions principal token header parameter.
     */
    @WithName("token-header-param")
    @WithDefault("${tkit.rs.context.token.header-param:apm-principal-token}")
    String principalTokenHeaderParam();

    /**
     * Permissions resource action separator.
     */
    @WithName("key-separator")
    @WithDefault("#")
    String keySeparator();

    /**
     * Mock configuration
     */
    @WithName("mock")
    MockConfig mock();

    interface MockConfig {

        /**
         * Enable or disable mock service
         */
        @WithName("enabled")
        @WithDefault("false")
        boolean enabled();

        /**
         * Mock data for role
         * Map format : <role>.<permissions>
         */
        @WithName("roles")
        Map<String, Map<String, List<String>>> roles();
    }

}
