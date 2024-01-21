package io.github.onecx.quarkus.tenant;

public class TenantException extends RuntimeException {

    private Enum<?> key;

    public TenantException(Enum<?> key, String message) {
        super(message);
    }

    public Enum<?> getKey() {
        return key;
    }

}
