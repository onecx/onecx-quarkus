package org.tkit.onecx.quarkus.parameter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

public class MicrometerMetricsRecorder implements MetricsRecorder {

    @Override
    public void increase(String name) {
        Counter.builder("onecx_parameters_item")
                .description("Number of times parameter have been used")
                .tag(TAG_NAME, name)
                .register(Metrics.globalRegistry)
                .increment();
    }

    @Override
    public void update(String tenant, String type, String status) {
        Counter.builder("onecx_parameters_update")
                .description("Number of times tenant parameters have been updated")
                .tag(TAG_TENANT, tenant)
                .tag(TAG_TYPE, type)
                .tag(TAG_STATUS, status)
                .register(Metrics.globalRegistry)
                .increment();
    }

    @Override
    public void history(String tenant, String status) {
        Counter.builder("onecx_parameters_history")
                .description("Number of times parameters history have been send")
                .tag(TAG_TENANT, tenant)
                .tag(TAG_STATUS, status)
                .register(Metrics.globalRegistry)
                .increment();
    }
}
