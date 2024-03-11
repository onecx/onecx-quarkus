package org.tkit.onecx.quarkus.tenant;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Tenant extension configuration
 */
@ConfigMapping(prefix = "onecx.tenant")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface TenantConfig {

    /**
     * Enable or disable cache for the token
     */
    @WithName(value = "cache-enabled")
    @WithDefault(value = "true")
    boolean cacheEnabled();

    /**
     * Tenant client URL configuration.
     * This property is alias for rest-client generated configuration property `quarkus.rest-client.onecx_tenant.url`
     */
    @WithName(value = "service.client.url")
    @WithDefault(value = "http://onecx-tenant:8080")
    String clientUrl();
}
