package org.tkit.onecx.quarkus.parameter.config;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.parameters")
public interface ParametersConfig {

    /**
     * Parameters client URL configuration.
     * This property is alias for rest-client generated configuration property `quarkus.rest-client.onecx_permission.url`
     */
    @WithName("service.client.url")
    @WithDefault("http://onecx-parameter-svc:8080")
    String clientUrl();

    /**
     * If set to true, the application will attempt to look up the configuration from Consul
     */
    @WithName("enabled")
    @WithDefault("true")
    boolean enabled();

    /**
     * Cache configuration
     */
    @WithName("cache")
    CacheConfig cache();

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
     * Instance ID
     */
    @WithName("instance-id")
    Optional<String> instanceId();

    /**
     * History configuration.
     */
    @WithName("history")
    HistoryConfig history();

    /**
     * Parameters configuration
     */
    @WithName("items")
    Map<String, String> parameters();

    /**
     * Multi-tenancy configuration.
     */
    @WithName("tenant")
    TenantConfig tenant();

    /**
     * Cache configuration.
     */
    interface CacheConfig {
        /**
         * Enable or disable client cache.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        /**
         * Scheduler configuration
         */
        @WithName("update-interval-in-milliseconds")
        @WithDefault("30000")
        long updateIntervalInMilliseconds();

        /**
         * Pull parameters during start phase
         */
        @WithName("update-at-start")
        @WithDefault("false")
        boolean updateAtStart();
    }

    /**
     * History configuration
     */
    interface HistoryConfig {

        /**
         * If set to true, the application will send history information to the parameter management.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        /**
         * Update history interval in milliseconds
         */
        @WithName("update-interval-in-milliseconds")
        @WithDefault("300000")
        Long updateIntervalInMilliseconds();

    }

    /**
     * Multi-tenancy configuration.
     */
    interface TenantConfig {

        /**
         * Enable or disable multi-tenancy.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        /**
         * Default tenant id.
         */
        @WithName("default-tenant")
        @WithDefault("default")
        String defaultTenant();

    }

}
