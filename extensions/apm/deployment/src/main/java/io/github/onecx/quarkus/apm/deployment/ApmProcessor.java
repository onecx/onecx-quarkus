package io.github.onecx.quarkus.apm.deployment;

import io.github.onecx.quarkus.apm.ApmConfig;
import io.github.onecx.quarkus.apm.ApmRecorder;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.ApplicationConfig;

public class ApmProcessor {

    public static final String FEATURE = "onecx-apm";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public void configure(BeanContainerBuildItem beanContainer, ApmRecorder recorder, ApmConfig config,
            ApplicationConfig appConfig) {
        BeanContainer container = beanContainer.getValue();
        recorder.init(container, appConfig, config);
    }
}
