package io.github.onecx.quarkus.tenant;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "onecx.tenant")
public interface TenantConfig {

    /**
     * Token header id
     */
    @WithName(value = "token-header-param")
    @WithDefault(value = "apm-principal-token")
    String tokenHeaderParam();
}
