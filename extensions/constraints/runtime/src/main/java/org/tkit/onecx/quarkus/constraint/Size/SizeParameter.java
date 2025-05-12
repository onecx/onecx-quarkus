package org.tkit.onecx.quarkus.constraint.Size;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SizeParameter {

    private Long max;

    private Long min;

    private String message;

    private String errorCode;

    private String key;

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(Long min) {
        this.min = min;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
