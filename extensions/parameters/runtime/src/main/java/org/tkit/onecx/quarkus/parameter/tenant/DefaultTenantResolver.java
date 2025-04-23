package org.tkit.onecx.quarkus.parameter.tenant;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;

@Unremovable
@DefaultBean
@ApplicationScoped
public class DefaultTenantResolver implements TenantResolver {

}
