package org.tkit.onecx.quarkus.parameter.config;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.parameters")
public interface ParametersConfig {

    /**
     * Parameters client URL configuration.
     * This property is alias for rest-client generated configuration property `quarkus.rest-client.onecx_parameter.url`
     */
    @WithName("service.client.url")
    @WithDefault("http://onecx-parameter-svc:8080")
    String clientUrl();

    /**
     * If set to true, the application will attempt to look up the configuration from Parameter service
     */
    @WithName("enabled")
    @WithDefault("true")
    boolean enabled();

    /**
     * Throw update exception when parameters are loaded from backend.
     */
    @WithName("throw-update-exception")
    @WithDefault("false")
    boolean throwUpdateException();

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
     * Parameters application ID.
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
         * Update parameter scheduler configuration in milliseconds.
         */
        @WithName("update-schedule")
        @WithDefault("900000")
        Long updateSchedule();

        /**
         * Pull parameters during start phase
         */
        @WithName("update-at-start")
        @WithDefault("false")
        boolean updateAtStart();

        /**
         * Does not start the microservices
         * if an error occurs while retrieving the parameters during the startup phase.
         */
        @WithName("failed-at-start")
        @WithDefault("false")
        boolean failedAtStart();
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
         * Update history scheduler configuration in milliseconds.
         */
        @WithName("update-schedule")
        @WithDefault("900000")
        Long updateSchedule();

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

    }

}
