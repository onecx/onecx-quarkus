package org.tkit.onecx.quarkus.parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.DefaultBean;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
@DefaultBean
public class DefaultParameterValueMapper implements ParameterValueMapper {

    @Inject
    ObjectMapper mapper;

    @Override
    public <T> T from(String value, Class<T> clazz) {
        try {
            return mapper.readValue(value, clazz);
        } catch (IOException ex) {
            throw new ReadValueException(
                    "The given string value: " + value + " cannot be transformed to Json object: " + clazz,
                    ex);
        }
    }
}
