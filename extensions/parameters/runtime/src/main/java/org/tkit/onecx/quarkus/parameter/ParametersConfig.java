package org.tkit.onecx.quarkus.parameter;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "parameters", phase = ConfigPhase.RUN_TIME, prefix = "onecx")
public class ParametersConfig {

    public static final String HOST = "quarkus.rest-client.onecx-parameters.url";

    /**
     * If set to true, the application will attempt to look up the configuration from Consul
     */
    @ConfigItem(defaultValue = "true")
    boolean enabled;

    /**
     * Scheduler configuration
     */
    @ConfigItem(defaultValue = "30000")
    long updateIntervalInMilliseconds;

    /**
     * Pull parameters during start phase
     */
    @ConfigItem(defaultValue = "false")
    boolean updateAtStart = false;

    /**
     * Application ID
     */
    @ConfigItem(name = "application-id")
    Optional<String> applicationId;

    /**
     * Instance ID
     */
    @ConfigItem(name = "instance-id")
    Optional<String> instanceId;

    /**
     * Metrics configuration.
     */
    @ConfigItem(name = "metrics")
    MetricsConfig metrics;

    /**
     * Metrics configuration
     */
    @ConfigGroup
    public static class MetricsConfig {

        /**
         * If set to true, the application will send metrics information to the parameter management.
         */
        @ConfigItem(defaultValue = "true")
        boolean enabled;

        /**
         * Metrics scheduler configuration
         */
        @ConfigItem(defaultValue = "20000")
        Long metricsIntervalInMilliseconds;

    }

}
