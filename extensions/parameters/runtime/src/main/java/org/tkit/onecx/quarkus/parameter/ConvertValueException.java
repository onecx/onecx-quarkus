package org.tkit.onecx.quarkus.parameter;

public class ConvertValueException extends RuntimeException {

    public ConvertValueException(String message, Throwable e) {
        super(message, e);
    }
}
