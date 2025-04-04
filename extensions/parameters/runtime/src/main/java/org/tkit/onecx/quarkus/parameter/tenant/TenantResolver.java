package org.tkit.onecx.quarkus.parameter.tenant;

import org.tkit.quarkus.context.Context;

public interface TenantResolver {

    default Context getTenantContext(Context ctx) {
        return ctx;
    }

}
