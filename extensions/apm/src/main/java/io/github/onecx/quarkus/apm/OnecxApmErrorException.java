package io.github.onecx.quarkus.apm;

public class OnecxApmErrorException extends RuntimeException {

    private Enum<?> key;

    public OnecxApmErrorException(Enum<?> key, String message) {
        super(message);
        this.key = key;
    }

    public Enum<?> getKey() {
        return key;
    }
}
