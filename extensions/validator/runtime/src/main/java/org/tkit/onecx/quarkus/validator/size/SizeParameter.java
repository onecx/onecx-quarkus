package org.tkit.onecx.quarkus.validator.size;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SizeParameter {

    private Long max;

    private Long min;

    private String message;

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

}
