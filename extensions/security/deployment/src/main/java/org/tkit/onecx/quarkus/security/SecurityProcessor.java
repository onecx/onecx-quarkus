package org.tkit.onecx.quarkus.security;

import java.util.*;

import org.jboss.jandex.*;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.BeanArchiveIndexBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
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

    @BuildStep
    void build(BuildProducer<FeatureBuildItem> feature) {
        feature.produce(new FeatureBuildItem(FEATURE_NAME));
    }

    @BuildStep
    @Record(ExecutionTime.STATIC_INIT)
    void buildImplPermissions(SecurityBuildTimeConfig config, BeanArchiveIndexBuildItem ci,
            SecurityCheckRecorder recorder,
            BuildProducer<AdditionalSecurityCheckBuildItem> additionalSecurityChecks) {

        if (!config.mapping.enabled) {
            return;
        }

        var index = ci.getIndex();
        List<AnnotationInstance> permissionInstances = new ArrayList<>(
                index.getAnnotationsWithRepeatable(DotNames.PERMISSIONS_ALLOWED, index));

        if (!permissionInstances.isEmpty()) {

            Map<MethodInfo, AnnotationInstance> methodToInstanceCollector = new HashMap<>();
            Map<ClassInfo, AnnotationInstance> classAnnotations = new HashMap<>();

            var securityChecks = new CopyPermissionSecurityChecks.PermissionSecurityChecksBuilder(recorder)
                    .gatherPermissionsAllowedAnnotations(permissionInstances, methodToInstanceCollector, classAnnotations)
                    .validatePermissionClasses(index)
                    .createPermissionPredicates()
                    .build().get();

            if (!securityChecks.isEmpty()) {

                for (Map.Entry<MethodInfo, SecurityCheck> e : securityChecks.entrySet()) {

                    var methodInfo = e.getKey();
                    var tmp = methodInfo.declaringClass();
                    // skip wrong package
                    if (isNotPackage(config, tmp.name())) {
                        continue;
                    }
                    if (!tmp.isInterface()) {
                        continue;
                    }

                    var classes = index.getKnownDirectImplementors(tmp.name());
                    for (ClassInfo clas : classes) {
                        // skip wrong package
                        if (isNotPackage(config, clas.name())) {
                            continue;
                        }
                        var method = clas.method(methodInfo.name(), methodInfo.parameterTypes().toArray(new Type[0]));
                        if (method != null) {

                            if (method.hasAnnotation(DotNames.PERMISSIONS_ALLOWED)) {
                                continue;
                            }
                            additionalSecurityChecks.produce(new AdditionalSecurityCheckBuildItem(method, e.getValue()));
                        }
                    }
                }
            }
        }

    }

    @BuildStep
    AnnotationsTransformerBuildItem transform(SecurityBuildTimeConfig config, CombinedIndexBuildItem ci) {

        if (!config.mapping.enabled) {
            return new AnnotationsTransformerBuildItem(transformationContext -> {
            });
        }

        var index = ci.getIndex();

        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {

            public boolean appliesTo(AnnotationTarget.Kind kind) {
                return kind == AnnotationTarget.Kind.METHOD;
            }

            public void transform(TransformationContext context) {
                var method = context.getTarget().asMethod();
                var classInfo = method.declaringClass();
                if (method.hasAnnotation(DotNames.PERMISSIONS_ALLOWED)) {
                    return;
                }
                if (classInfo.isInterface()) {
                    return;
                }

                // skip wrong package
                if (isNotPackage(config, classInfo.name())) {
                    return;
                }

                AnnotationInstance ai = null;
                List<DotName> interfaces = classInfo.interfaceNames();
                for (DotName item : interfaces) {
                    var ite = index.getClassByName(item);
                    if (ite == null) {
                        continue;
                    }

                    // skip wrong package
                    if (isNotPackage(config, ite.name())) {
                        return;
                    }

                    var m = ite.method(method.name(), method.parameterTypes().toArray(new Type[0]));
                    if (m == null) {
                        continue;
                    }

                    ai = m.annotation(DotNames.PERMISSIONS_ALLOWED);
                    if (ai != null) {
                        break;
                    }
                }

                if (ai == null) {
                    return;
                }

                List<AnnotationValue> values = ai.value().asArrayList().stream()
                        .map(x -> AnnotationValue.createStringValue(x.name(), x.asString()))
                        .toList();
                context.transform().add(DotNames.PERMISSIONS_ALLOWED, AnnotationValue.createArrayValue("value", values)).done();
            }
        });
    }

    private static boolean isNotPackage(SecurityBuildTimeConfig config, DotName name) {
        var n = name.toString();
        Optional<String> add = config.mapping.packages.stream().filter(n::startsWith).findFirst();
        return add.isEmpty();
    }

}
