package org.tkit.onecx.quarkus.constraint.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class ConstraintProcessor {

    public static final String FEATURE = "onecx-constraints";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

}
