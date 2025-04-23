package org.tkit.onecx.quarkus.parameter.runtime;

import java.util.Map;

import org.tkit.quarkus.context.Context;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class TenantParameters {

    private final Context ctx;

    private Map<String, Object> parameters;

    public TenantParameters(Context ctx, Map<String, Object> parameters) {
        this.ctx = ctx;
        this.parameters = parameters;
    }

    public Context getCtx() {
        return ctx;
    }

    public void updateParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
