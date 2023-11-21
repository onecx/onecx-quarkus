package io.github.onecx.quarkus.tenant;

public class OnecxTenantException extends RuntimeException {

    private Enum<?> key;

    public OnecxTenantException(Enum<?> key, String message) {
        super(message);
    }

    public OnecxTenantException(Enum<?> key, String message, Throwable throwable) {
        super(message, throwable);
    }

    public Enum<?> getKey() {
        return key;
    }

}
