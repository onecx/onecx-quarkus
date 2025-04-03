package org.tkit.onecx.quarkus.parameter;

public class TenantException extends RuntimeException {

    public TenantException(String message) {
        super(message);
    }

    public TenantException(Throwable throwable) {
        super(throwable);
    }
}
