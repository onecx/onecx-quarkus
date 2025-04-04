package org.tkit.onecx.quarkus.parameter.history;

import org.tkit.quarkus.context.Context;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParametersHistoryEvent {

    public static final String NAME = "ParametersHistoryEvent";

    String name;

    Class<?> type;

    Object defaultValue;

    Object value;

    Context ctx;

    public static ParametersHistoryEvent of(Context ctx, String name, Class<?> type, Object value,
            Object defaultValue) {
        ParametersHistoryEvent e = new ParametersHistoryEvent();
        e.name = name;
        e.type = type;
        e.defaultValue = defaultValue;
        e.value = value;
        e.ctx = ctx;
        return e;
    }
}
