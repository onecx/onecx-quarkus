package org.tkit.onecx.quarkus.it.parameter;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.quarkus.rs.context.tenant.TenantCustomResolver;

import io.quarkus.arc.Unremovable;

@Unremovable
@RequestScoped
public class TenantResolver implements TenantCustomResolver {

    private static final Logger log = LoggerFactory.getLogger(TenantResolver.class);

    @ConfigProperty(name = "tkit.rs.context.tenant-id.mock.claim-org-id", defaultValue = "orgId")
    @Inject
    String claimId;

    @Override
    public String getTenantId(JsonWebToken principalToken, ContainerRequestContext containerRequestContext) {
        String tenantId = principalToken.getClaim(claimId);
        log.info("Tenant resolver for parameters, tenant-id: {}", tenantId);
        return tenantId;
    }
}
