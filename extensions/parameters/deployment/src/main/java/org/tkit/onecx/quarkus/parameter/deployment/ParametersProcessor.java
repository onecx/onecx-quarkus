package org.tkit.onecx.quarkus.parameter.deployment;

import java.util.Optional;

import org.tkit.onecx.quarkus.parameter.config.ParametersBuildTimeConfig;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.metrics.MicrometerMetricsRecorder;
import org.tkit.onecx.quarkus.parameter.metrics.NoopMetricsRecorder;
import org.tkit.onecx.quarkus.parameter.runtime.ParametersRecorder;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.runtime.metrics.MetricsFactory;
import io.quarkus.smallrye.health.deployment.spi.HealthBuildItem;

public class ParametersProcessor {

    public static final String FEATURE = "onecx-parameters";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public void configure(BeanContainerBuildItem beanContainer, ParametersRecorder recorder, ParametersConfig config) {
        BeanContainer container = beanContainer.getValue();
        recorder.configSources(container, config);
    }

    @BuildStep
    void addHealthCheck(ParametersBuildTimeConfig config, BuildProducer<HealthBuildItem> healthChecks) {
        //        healthChecks.produce(new HealthBuildItem(ZeebeHealthCheck.class.getName(), config.health().enabled()));
    }

    @BuildStep
    void addMetrics(ParametersBuildTimeConfig config, Optional<MetricsCapabilityBuildItem> metricsCapability,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        if (!config.metrics().enabled()) {
            return;
        }
        if (metricsCapability.isPresent()) {
            boolean withMicrometer = metricsCapability.map(cap -> cap.metricsSupported(MetricsFactory.MICROMETER))
                    .orElse(false);
            if (withMicrometer) {
                additionalBeans.produce(new AdditionalBeanBuildItem.Builder().addBeanClass(MicrometerMetricsRecorder.class)
                        .setDefaultScope(BuiltinScope.APPLICATION.getName())
                        .setUnremovable()
                        .build());
                return;
            }
        }
        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(NoopMetricsRecorder.class));
    }

}
