package io.github.onecx.quarkus.apm;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "onecx.apm")
public interface ApmConfig {

    @WithName(value = "enabled")
    @WithDefault(value = "true")
    boolean enabled();

    @WithName(value = "name")
    @WithDefault(value = "apm")
    String name();

    @WithName(value = "token-header-param")
    @WithDefault(value = "apm-principal-token")
    String tokenHeaderParam();

    @WithName(value = "application-id")
    @WithDefault(value = "${quarkus.application.name}")
    String applicationId();

    @WithName(value = "debug-log")
    @WithDefault(value = "false")
    boolean debugLog();

    @WithName(value = "client.v3")
    ClientConfigV3 clientV3();

    @WithName(value = "client.version")
    @WithDefault("v3")
    String clientVersion();

    interface ClientConfigV3 {

        @WithName(value = "context-action-separator")
        @WithDefault(value = "#")
        String separator();

    }
}
