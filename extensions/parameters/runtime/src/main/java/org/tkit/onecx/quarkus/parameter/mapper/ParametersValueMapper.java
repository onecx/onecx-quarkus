package org.tkit.onecx.quarkus.parameter.mapper;

public interface ParametersValueMapper {

    <T> T toType(final Object value, final Class<T> clazz) throws ReadValueException;

    Object toMap(final String value) throws ReadValueException;

    class ReadValueException extends RuntimeException {

        public ReadValueException(String message, Throwable e) {
            super(message, e);
        }
    }
}
