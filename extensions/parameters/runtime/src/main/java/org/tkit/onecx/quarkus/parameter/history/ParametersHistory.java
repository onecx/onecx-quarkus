package org.tkit.onecx.quarkus.parameter.history;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParametersHistory {

    private final Map<String, ParameterInfoItem> parameters = new ConcurrentHashMap<>();

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

    public void addParameterRequest(String name, Object defaultValue, Object currentValue) {
        getParameters()
                .computeIfAbsent(name, s -> new ParametersHistory.ParameterInfoItem(defaultValue, currentValue))
                .getCount().incrementAndGet();
    }

    public Map<String, ParameterInfoItem> getParameters() {
        return parameters;
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
        return parameters.isEmpty();
    }

    static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneId.of("UTC"));
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
