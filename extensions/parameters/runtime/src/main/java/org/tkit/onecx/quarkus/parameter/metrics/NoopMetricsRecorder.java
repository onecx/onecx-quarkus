package org.tkit.onecx.quarkus.parameter.metrics;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NoopMetricsRecorder implements MetricsRecorder {

    public void increase(String name) {

    }

    @Override
    public void update(String tenant, String type, String status) {

    }

    @Override
    public void history(String tenant, String status) {

    }
}
