package org.tkit.onecx.quarkus.security;

import java.util.*;

import org.jboss.jandex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.security.deployment.AdditionalSecurityCheckBuildItem;
import io.quarkus.security.deployment.DotNames;
import io.quarkus.security.runtime.SecurityCheckRecorder;
import io.quarkus.security.spi.runtime.SecurityCheck;

public class SecurityProcessor {

    static final String FEATURE_NAME = "onecx-security";

    private static final Logger log = LoggerFactory.getLogger(SecurityProcessor.class);

    @BuildStep
    void build(BuildProducer<FeatureBuildItem> feature) {
        feature.produce(new FeatureBuildItem(FEATURE_NAME));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void buildImplPermissions(SecurityBuildTimeConfig config, BeanArchiveIndexBuildItem ci,
            SecurityCheckRecorder recorder,
            BuildProducer<AdditionalSecurityCheckBuildItem> additionalSecurityChecks) {

        if (!config.mapping().enabled()) {
            log.info("Permissions annotation build check is disabled");
            return;
        }

        var index = ci.getIndex();
        var permissionInstances = new ArrayList<>(
                index.getAnnotationsWithRepeatable(DotNames.PERMISSIONS_ALLOWED, index));

        if (permissionInstances.isEmpty()) {
            return;
        }

        Map<MethodInfo, AnnotationInstance> methodToInstanceCollector = new HashMap<>();
        Map<ClassInfo, AnnotationInstance> classAnnotations = new HashMap<>();

        var securityChecks = new CopyPermissionSecurityChecks.PermissionSecurityChecksBuilder(recorder)
                .gatherPermissionsAllowedAnnotations(permissionInstances, methodToInstanceCollector, classAnnotations)
                .validatePermissionClasses(index)
                .createPermissionPredicates()
                .build().get();

        if (securityChecks.isEmpty()) {
            return;
        }

        buildSecurityChecks(securityChecks, additionalSecurityChecks, config, index);

    }

    private void buildSecurityChecks(Map<MethodInfo, SecurityCheck> securityChecks,
            BuildProducer<AdditionalSecurityCheckBuildItem> additionalSecurityChecks, SecurityBuildTimeConfig config,
            IndexView index) {

        for (Map.Entry<MethodInfo, SecurityCheck> e : securityChecks.entrySet()) {

            var methodInfo = e.getKey();
            var tmp = methodInfo.declaringClass();
            // skip wrong package
            if (!tmp.isInterface() || isNotPackage(config, tmp.name())) {
                continue;
            }

            var classes = index.getKnownDirectImplementors(tmp.name());
            for (ClassInfo clas : classes) {
                // skip wrong package
                if (isNotPackage(config, clas.name())) {
                    continue;
                }
                var method = clas.method(methodInfo.name(), methodInfo.parameterTypes().toArray(new Type[0]));
                if (method != null && !method.hasAnnotation(DotNames.PERMISSIONS_ALLOWED)) {
                    additionalSecurityChecks.produce(new AdditionalSecurityCheckBuildItem(method, e.getValue()));
                }
            }
        }
    }

    @BuildStep
    void transform(SecurityBuildTimeConfig config, CombinedIndexBuildItem ci,
            BuildProducer<AnnotationsTransformerBuildItem> transformer) {

        if (!config.mapping().enabled()) {
            log.info("Security annotation mapping is disabled");
            return;
        }

        var index = ci.getIndex();

        transformer.produce(new AnnotationsTransformerBuildItem(AnnotationTransformation.forMethods()
                .whenMethod(m -> !m.hasAnnotation(DotNames.PERMISSIONS_ALLOWED)
                        && !m.declaringClass().isInterface()
                        && !isNotPackage(config, m.declaringClass().name()))
                .transform(context -> {

                    var ai = findAnnotationInstance(context.declaration().asMethod(), index, config);
                    if (ai == null) {
                        return;
                    }

                    List<AnnotationValue> values = ai.value().asArrayList().stream()
                            .map(x -> AnnotationValue.createStringValue(x.name(), x.asString()))
                            .toList();
                    context.add(AnnotationInstance.builder(DotNames.PERMISSIONS_ALLOWED)
                            .add(AnnotationValue.createArrayValue("value", values)).build());
                })));

    }

    private static boolean isNotPackage(SecurityBuildTimeConfig config, DotName name) {
        var n = name.toString();
        Optional<String> add = config.mapping().packages().stream().filter(n::startsWith).findFirst();
        return add.isEmpty();
    }

    private static AnnotationInstance findAnnotationInstance(MethodInfo method, IndexView index,
            SecurityBuildTimeConfig config) {
        var classInfo = method.declaringClass();
        List<DotName> interfaces = classInfo.interfaceNames();
        for (DotName item : interfaces) {
            var ite = index.getClassByName(item);
            if (ite == null || isNotPackage(config, ite.name())) {
                continue;
            }

            var m = ite.method(method.name(), method.parameterTypes().toArray(new Type[0]));
            if (m != null) {
                var ai = m.annotation(DotNames.PERMISSIONS_ALLOWED);
                if (ai != null) {
                    return ai;
                }
            }
        }

        return null;
    }

}
