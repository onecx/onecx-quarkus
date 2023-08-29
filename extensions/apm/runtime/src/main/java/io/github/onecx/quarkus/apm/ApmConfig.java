package io.github.onecx.quarkus.apm;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "apm", phase = ConfigPhase.RUN_TIME, prefix = "onecx")
public class ApmConfig {

    public static final String HOST = "quarkus.rest-client.onecx-amp.url";

    /**
     * If set to true, the application will attempt to enable APM service.
     */
    @ConfigItem(defaultValue = "true")
    boolean enabled;

    /**
     * Parameters service host name.
     */
    @ConfigItem(name = "application-id")
    Optional<String> applicationId;

    /**
     * Parameters service API version.
     */
    @ConfigItem(name = "api-version", defaultValue = "v3")
    String apiVersion;

    /**
     * Parameters service action separator.
     */
    @ConfigItem(name = "action-separator", defaultValue = "#")
    String actionSeparator;

    /**
     * If set to true, the application will log user actions
     */
    @ConfigItem(defaultValue = "true")
    boolean log;

    /**
     * Parameters service action separator.
     */
    @ConfigItem(name = "header-apm-principal-token-name")
    Optional<String> headerApmPrincipalTokenName;
}
