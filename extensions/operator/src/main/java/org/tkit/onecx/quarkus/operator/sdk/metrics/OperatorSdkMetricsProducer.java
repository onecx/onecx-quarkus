package org.tkit.onecx.quarkus.operator.sdk.metrics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.operator.OperatorConfig;

import io.javaoperatorsdk.operator.api.monitoring.Metrics;
import io.javaoperatorsdk.operator.monitoring.micrometer.MicrometerMetrics;
import io.micrometer.core.instrument.MeterRegistry;

public class OperatorSdkMetricsProducer {

    private static final Logger log = LoggerFactory.getLogger(OperatorSdkMetricsProducer.class);

    @Inject
    OperatorConfig config;

    @Inject
    MeterRegistry registry;

    @Produces
    @ApplicationScoped
    public Metrics operatorSdkMetrics() {
        if (!config.sdkMetrics().enabled()) {
            log.info("Operator sdk metrics disabled → using Metrics.NOOP");
            return Metrics.NOOP;
        }

        log.info("Operator sdk metrics enabled → using default MicrometerMetrics");
        return MicrometerMetrics.newMicrometerMetricsBuilder(registry).build();
    }
}
