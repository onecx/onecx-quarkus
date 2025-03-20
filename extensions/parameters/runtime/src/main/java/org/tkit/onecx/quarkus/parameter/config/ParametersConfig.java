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
     * Metrics configuration.
     */
    @WithName("metrics")
    MetricsConfig metrics();

    /**
     * Parameters configuration
     */
    @WithName("keys")
    Map<String, Parameter> parameters();

    interface Parameter {

        /**
         * Parameter value
         */
        @WithName("value")
        Optional<String> value();

        /**
         * Parameter description
         */
        @WithName("description")
        Optional<String> description();

    }

    /**
     * Metrics configuration
     */
    interface MetricsConfig {

        /**
         * If set to true, the application will send metrics information to the parameter management.
         */
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        /**
         * Metrics scheduler configuration
         */
        @WithName("metrics-interval-in-milliseconds")
        @WithDefault("300000")
        Long metricsIntervalInMilliseconds();

    }

}
