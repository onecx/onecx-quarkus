package org.tkit.onecx.quarkus.validator.deployment;

import org.tkit.onecx.quarkus.validator.service.NoopValueService;
import org.tkit.onecx.quarkus.validator.service.ParameterValueService;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class ValidatorProcessor {

    static final String CAPABILITY_PARAMETERS = "org.tkit.onecx.parameters";

    public static final String FEATURE = "onecx-validator";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void valueServiceInitialize(Capabilities capabilities, BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        if (capabilities.isCapabilityWithPrefixPresent(CAPABILITY_PARAMETERS)) {
            additionalBeans.produce(new AdditionalBeanBuildItem.Builder().addBeanClass(ParameterValueService.class)
                    .setDefaultScope(BuiltinScope.APPLICATION.getName())
                    .setUnremovable()
                    .build());
            return;
        }
        additionalBeans.produce(new AdditionalBeanBuildItem.Builder().addBeanClass(NoopValueService.class)
                .setDefaultScope(BuiltinScope.APPLICATION.getName())
                .setUnremovable()
                .build());
    }

}
