package org.tkit.onecx.quarkus.parameter.tenant;

public interface ParametersTenantResolver {

    String defaultTenant();

    String getTenant();

}
