package io.github.onecx.quarkus.parameter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParametersBucket {

    public Map<String, ParameterInfo> parameters = new ConcurrentHashMap<>();

    public String instanceId;

    public OffsetDateTime start = now();

    public OffsetDateTime end;

    /**
     * Add a new request to the bucket, this will increase the count of requests
     * for the parameter.
     *
     * @param name the name of the parameter requested
     * @param clazz the class/type of the parameter requested
     * @param defaultValue the default value of the parameter requested
     */
    public void addParameterRequest(String name, Class<?> clazz, Object defaultValue, Object currentValue) {
        parameters.computeIfAbsent(name, s -> new ParameterInfo(clazz, defaultValue, currentValue)).count.incrementAndGet();
    }

    public void end() {
        end = now();
    }

    static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneId.of("UTC"));
    }

    @RegisterForReflection
    public static class ParameterInfo {

        public AtomicLong count;

        public String type;

        public Object defaultValue;

        public Object currentValue;

        ParameterInfo(Class<?> type, Object defaultValue, Object currentValue) {
            this.count = new AtomicLong(0);
            this.type = type.getSimpleName();
            this.defaultValue = defaultValue;
            this.currentValue = currentValue;
        }

    }
}
