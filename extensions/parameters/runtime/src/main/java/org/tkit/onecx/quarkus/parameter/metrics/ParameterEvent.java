package org.tkit.onecx.quarkus.parameter.metrics;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParameterEvent {
    public static final String NAME = "ParameterEvent";

    String name;

    Class<?> type;

    String defaultValue;

    Object value;

    public static ParameterEvent of(String name, Class<?> type, String defaultValue, Object value) {
        ParameterEvent e = new ParameterEvent();
        e.name = name;
        e.type = type;
        e.defaultValue = defaultValue;
        e.value = value;
        return e;
    }
}
