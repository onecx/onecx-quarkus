package org.tkit.onecx.quarkus.parameter.metrics;

public interface MetricsRecorder {

    void increase(String name);
}
