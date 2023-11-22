package io.github.onecx.quarkus.it.parameter.deployment;

import io.github.onecx.quarkus.it.parameter.ParametersConfig;
import io.github.onecx.quarkus.it.parameter.ParametersRecorder;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.ApplicationConfig;

public class ParametersProcessor {

    public static final String FEATURE = "onecx-parameters";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    public void configure(BeanContainerBuildItem beanContainer, ParametersRecorder recorder, ParametersConfig config,
            ApplicationConfig appConfig) {
        BeanContainer container = beanContainer.getValue();
        recorder.configSources(container, appConfig, config);
    }
}
