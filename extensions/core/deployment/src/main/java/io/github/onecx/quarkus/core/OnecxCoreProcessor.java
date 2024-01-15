package io.github.onecx.quarkus.core;

import java.util.List;

import org.jboss.jandex.*;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;

public class OnecxCoreProcessor {

    @BuildStep
    AnnotationsTransformerBuildItem transform(ApplicationIndexBuildItem index) {

        var anno = DotName.createSimple("io.quarkus.security.PermissionsAllowed");
        return new AnnotationsTransformerBuildItem(new AnnotationsTransformer() {

            public boolean appliesTo(org.jboss.jandex.AnnotationTarget.Kind kind) {
                return kind == AnnotationTarget.Kind.METHOD;
            }

            public void transform(TransformationContext context) {
                var method = context.getTarget().asMethod();
                var classInfo = method.declaringClass();
                if (method.hasAnnotation(anno)) {
                    System.out.println("!!!HAS ANNO #### -> " + classInfo.name() + " -> " + method + " ----> "
                            + method.annotation(anno).value());
                    return;
                }

                if (!classInfo.name().toString().startsWith("io.github.onecx.quarkus.it")) {
                    return;
                }

                AnnotationInstance ai = null;
                List<DotName> interfaces = classInfo.interfaceNames();
                for (DotName item : interfaces) {
                    var ite = index.getIndex().getClassByName(item);
                    //                    System.out.println("CHECK INTERFACE #### -> " + classInfo.name() + " -> " + item + " ----> " + ite);
                    if (ite == null) {
                        continue;
                    }

                    var m = ite.method(method.name(), method.parameterTypes().toArray(new Type[0]));
                    if (m == null) {
                        continue;
                    }
                    //                    System.out.println("CHECK METHOD #### -> " + classInfo.name() + " -> " + ite + " ----> " + m);

                    ai = m.annotation(anno);
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
                System.out.println("ADD ANNO: " + classInfo.name() + "." + method.name() + " -> " + values);
                context.transform().add(anno, AnnotationValue.createArrayValue("value", values)).done();

            }
        });
    }

}
