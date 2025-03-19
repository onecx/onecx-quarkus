package org.tkit.onecx.quarkus.parameter.metrics;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParametersBucketItem {

    private Map<String, ParameterInfoItem> parameters = new ConcurrentHashMap<>();

    private String instanceId;

    private OffsetDateTime start;

    private OffsetDateTime end;

    public Map<String, ParameterInfoItem> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ParameterInfoItem> parameters) {
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
    public static class ParameterInfoItem {

        private final AtomicLong count;

        private final String description;

        private final Object defaultValue;

        private final Object currentValue;

        ParameterInfoItem(Object defaultValue, Object currentValue, String description) {
            this.count = new AtomicLong(0);
            this.description = description;
            this.defaultValue = defaultValue;
            this.currentValue = currentValue;
        }

        public AtomicLong getCount() {
            return count;
        }

        public String getDescription() {
            return description;
        }

        public Object getCurrentValue() {
            return currentValue;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}
