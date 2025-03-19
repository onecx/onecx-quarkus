package org.tkit.onecx.quarkus.parameter.deployment;

import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.runtime.ParametersRecorder;

import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;

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
}
