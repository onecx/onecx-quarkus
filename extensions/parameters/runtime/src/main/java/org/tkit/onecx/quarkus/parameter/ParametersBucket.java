package org.tkit.onecx.quarkus.parameter;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParametersBucket {

    private Map<String, ParameterInfo> parameters = new ConcurrentHashMap<>();

    private String instanceId;

    private OffsetDateTime start;

    private OffsetDateTime end;

    public Map<String, ParameterInfo> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public void setEnd(OffsetDateTime end) {
        this.end = end;
    }

    public OffsetDateTime getStart() {
        return start;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @RegisterForReflection
    public static class ParameterInfo {

        private final AtomicLong count;

        private final String type;

        private final Object defaultValue;

        private final Object currentValue;

        ParameterInfo(Class<?> type, Object defaultValue, Object currentValue) {
            this.count = new AtomicLong(0);
            this.type = type.getSimpleName();
            this.defaultValue = defaultValue;
            this.currentValue = currentValue;
        }

        public AtomicLong getCount() {
            return count;
        }

        public String getType() {
            return type;
        }

        public Object getCurrentValue() {
            return currentValue;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}
