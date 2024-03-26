package org.tkit.onecx.quarkus.tenant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.tkit.quarkus.rs.context.tenant.TenantCustomResolver;
import org.tkit.quarkus.rs.context.token.TokenContextConfig;

import io.quarkus.arc.Unremovable;

@Unremovable
@RequestScoped
public class TenantResolver implements TenantCustomResolver {

    @Inject
    TenantService tenantService;

    @Inject
    TokenContextConfig config;

    @Override
    public String getTenantId(JsonWebToken principalToken, ContainerRequestContext containerRequestContext) {

        String rawToken = null;

        // check principal token
        if (principalToken != null) {
            rawToken = principalToken.getRawToken();
        }

        // fallback to header parameter
        if (rawToken == null) {
            rawToken = containerRequestContext.getHeaders().getFirst(config.token().tokenHeaderParam());
        }

        // check raw token
        if (rawToken == null) {
            throw new TenantException(ErrorKeys.ERROR_MISSING_PRINCIPAL_TOKEN,
                    "Principal token for tenant is mandatory.");
        }

        return tenantService.getTenant(rawToken);
    }

    public enum ErrorKeys {

        ERROR_MISSING_PRINCIPAL_TOKEN;
    }
}
