package org.tkit.onecx.quarkus.parameter.mapper;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.DefaultBean;

@Singleton
@DefaultBean
public class DefaultParametersValueMapper implements ParametersValueMapper {

    @Inject
    ObjectMapper mapper;

    @Override
    public <T> T toType(Object value, Class<T> clazz) throws ReadValueException {
        try {
            return mapper.convertValue(value, clazz);
        } catch (Exception ex) {
            throw new ReadValueException(
                    "The given value: " + value + " cannot be transformed to object: " + clazz, ex);
        }
    }

    @Override
    public Object toMap(String value) throws ReadValueException {
        if (value == null) {
            return null;
        }
        try {
            return mapper.readValue(value, Object.class);
        } catch (Exception ex) {
            throw new ReadValueException(
                    "The given string: " + value + " cannot be transformed to json map", ex);
        }
    }
}
