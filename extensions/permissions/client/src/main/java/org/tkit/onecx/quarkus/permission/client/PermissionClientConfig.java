package org.tkit.onecx.quarkus.permission.client;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "onecx.permissions")
public interface PermissionClientConfig {

    /**
     * Tenant client URL configuration.
     * This property is alias for rest-client generated configuration property `quarkus.rest-client.onecx_permission.url`
     */
    @WithName(value = "service.client.url")
    @WithDefault(value = "http://onecx-permission:8080")
    String clientUrl();
}
