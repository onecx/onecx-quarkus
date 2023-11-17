package io.github.onecx.quarkus.tenant;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
@Unremovable
@PersistenceUnitExtension
public class OnecxTenantResolver implements TenantResolver {

    @Inject
    RoutingContext context;

    @Inject
    TenantConfig config;

    @Override
    public String getDefaultTenantId() {
        return config.defaultTenant;
    }

    @Override
    public String resolveTenantId() {
        String tenantId = context.get(config.routingContextAttribute);
        if (tenantId != null) {
            return tenantId;
        }

        return null;
    }
}
