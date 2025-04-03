package org.tkit.onecx.quarkus.parameter.history;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.tkit.quarkus.context.Context;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParametersHistory {

    private final Map<String, TenantParameters> tenants = new ConcurrentHashMap<>();

    private final String instanceId;

    private final OffsetDateTime start;

    private OffsetDateTime end;

    public ParametersHistory(String instanceId) {
        this.instanceId = instanceId;
        this.start = now();
    }

    public void end() {
        this.end = now();
    }

    public void addParameterRequest(ParametersHistoryEvent event) {
        tenants.computeIfAbsent(event.ctx.getTenantId(), t -> new TenantParameters(event.ctx))
                .addParameter(event.name, event.defaultValue, event.value);
    }

    public Map<String, TenantParameters> getTenants() {
        return tenants;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public boolean isEmpty() {
        return tenants.isEmpty();
    }

    static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    @RegisterForReflection
    public static class TenantParameters {

        private final Context ctx;

        private final Map<String, ParameterInfoItem> parameters = new ConcurrentHashMap<>();

        public TenantParameters(Context ctx) {
            this.ctx = ctx;
        }

        public Context getCtx() {
            return ctx;
        }

        public boolean isEmpty() {
            return parameters.isEmpty();
        }

        public Map<String, ParameterInfoItem> getParameters() {
            return parameters;
        }

        public void addParameter(String name, Object defaultValue, Object currentValue) {
            parameters.computeIfAbsent(name, k -> new ParametersHistory.ParameterInfoItem(defaultValue, currentValue))
                    .getCount().incrementAndGet();
        }
    }

    @RegisterForReflection
    public static class ParameterInfoItem {

        private final AtomicLong count;

        private final Object defaultValue;

        private final Object currentValue;

        ParameterInfoItem(Object defaultValue, Object currentValue) {
            this.count = new AtomicLong(0);
            this.defaultValue = defaultValue;
            this.currentValue = currentValue;
        }

        public AtomicLong getCount() {
            return count;
        }

        public Object getCurrentValue() {
            return currentValue;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}
