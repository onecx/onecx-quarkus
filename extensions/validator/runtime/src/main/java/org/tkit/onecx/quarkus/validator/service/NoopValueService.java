package org.tkit.onecx.quarkus.validator.service;

public class NoopValueService implements ValueService {

    @Override
    public String getName() {
        return "static";
    }

    @Override
    public <T> T getValue(String name, Class<T> type, T defaultValue) {
        return defaultValue;
    }
}
