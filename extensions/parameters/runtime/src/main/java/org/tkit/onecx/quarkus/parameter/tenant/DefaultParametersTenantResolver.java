package org.tkit.onecx.quarkus.parameter.tenant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.quarkus.context.ApplicationContext;

@Default
@RequestScoped
public class DefaultParametersTenantResolver implements ParametersTenantResolver {

    @Inject
    ParametersConfig config;

    @Override
    public String defaultTenant() {
        return config.tenant().defaultTenant();
    }

    @Override
    public String getTenant() {
        if (config.tenant().enabled()) {
            var tenant = ApplicationContext.get().getTenantId();
            if (tenant != null) {
                return tenant;
            }
        }
        return defaultTenant();
    }
}
