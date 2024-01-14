package io.github.onecx.quarkus.tenant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;

import org.tkit.quarkus.rs.context.tenant.RestCustomTenantResolver;

@RequestScoped
public class OnecxRestCustomTenantResolver implements RestCustomTenantResolver {

    @Inject
    OnecxTenantService tenantService;

    @Inject
    TenantConfig config;

    @Override
    public String getTenantId(ContainerRequestContext containerRequestContext) {

        // get token from header
        var token = containerRequestContext.getHeaders().getFirst(config.tokenHeaderParam());
        if (token == null) {
            throw new OnecxTenantException(ErrorKeys.ERROR_MISSING_HEADER_APM_TOKEN,
                    "Tenant token principal is mandatory. Header: " + config.tokenHeaderParam());
        }

        return tenantService.getTenant(token);
    }

    public enum ErrorKeys {

        ERROR_MISSING_HEADER_APM_TOKEN;
    }
}
