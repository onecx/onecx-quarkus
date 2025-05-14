package org.tkit.onecx.quarkus.validator.service;

public interface ValueService {

    String getName();

    <T> T getValue(String name, Class<T> type, T defaultValue);
}
