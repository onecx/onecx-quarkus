package org.tkit.onecx.quarkus.parameter;

public interface ParameterValueMapper {

    <T> T from(final String value, final Class<T> clazz);

    class ReadValueException extends RuntimeException {

        public ReadValueException(String message, Throwable e) {
            super(message, e);
        }
    }
}
