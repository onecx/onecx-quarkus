package org.tkit.onecx.quarkus.tenant;

public class TenantException extends RuntimeException {

    private final Enum<?> key;

    public TenantException(Enum<?> key, String message) {
        super(message);
        this.key = key;
    }

    public TenantException(Enum<?> key, String message, Throwable t) {
        super(message, t);
        this.key = key;
    }

    @SuppressWarnings("java:S1452")
    public Enum<?> getKey() {
        return key;
    }

}
