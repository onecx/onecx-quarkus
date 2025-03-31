package org.tkit.onecx.quarkus.parameter.history;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParametersHistoryEvent {

    public static final String NAME = "ParametersHistoryEvent";

    String name;

    Class<?> type;

    Object defaultValue;

    Object value;

    public static ParametersHistoryEvent of(String name, Class<?> type, Object defaultValue, Object value) {
        ParametersHistoryEvent e = new ParametersHistoryEvent();
        e.name = name;
        e.type = type;
        e.defaultValue = defaultValue;
        e.value = value;
        return e;
    }
}
