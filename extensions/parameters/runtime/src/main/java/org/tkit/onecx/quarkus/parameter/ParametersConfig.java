package org.tkit.onecx.quarkus.parameter;

import java.util.Optional;

import io.quarkus.runtime.annotations.*;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@StaticInitSafe
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.parameters")
public interface ParametersConfig {

    String HOST = "quarkus.rest-client.onecx-parameters.url";

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
        @WithDefault("20000")
        Long metricsIntervalInMilliseconds();

    }

}
