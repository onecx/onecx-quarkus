package io.github.onecx.quarkus.tenant;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "tenant-hibernate", phase = ConfigPhase.RUN_TIME, prefix = "onecx")
public class TenantConfig {

    @ConfigItem(name = "default-tenant", defaultValue = "default")
    String defaultTenant;

    @ConfigItem(name = "routing-context-attribute", defaultValue = "tenantId")
    String routingContextAttribute;
}
