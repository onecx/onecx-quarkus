package io.github.onecx.quarkus.parameter;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ParameterEvent {
    static final String NAME = "ParameterEvent";

    String propertyName;

    Class<?> propertyType;

    String defaultValue;

    String currentValue;

    public static ParameterEvent of(String propertyName, Class<?> propertyType, String defaultValue, String currentValue) {
        ParameterEvent e = new ParameterEvent();
        e.propertyName = propertyName;
        e.propertyType = propertyType;
        e.defaultValue = defaultValue;
        e.currentValue = currentValue;
        return e;
    }
}
