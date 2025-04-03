package org.tkit.onecx.quarkus.parameter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

public class MicrometerMetricsRecorder implements MetricsRecorder {

    @Override
    public void increase(String name) {
        Counter.builder("onecx_parameters")
                .description("Number of times parameter have been used")
                .tag("name", name)
                .register(Metrics.globalRegistry)
                .increment();
    }
}
