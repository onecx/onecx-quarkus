package org.tkit.onecx.quarkus.parameter.deployment;

import java.lang.reflect.Modifier;
import java.util.*;

import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;

import org.jboss.jandex.*;
import org.jboss.jandex.Type;
import org.tkit.onecx.quarkus.parameter.Parameter;
import org.tkit.onecx.quarkus.parameter.ParametersService;
import org.tkit.onecx.quarkus.parameter.config.ParametersBuildTimeConfig;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.metrics.MicrometerMetricsRecorder;
import org.tkit.onecx.quarkus.parameter.metrics.NoopMetricsRecorder;
import org.tkit.onecx.quarkus.parameter.runtime.AbstractParameterProducer;
import org.tkit.onecx.quarkus.parameter.runtime.ParametersRecorder;

import io.quarkus.arc.deployment.*;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CapabilityBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.metrics.MetricsCapabilityBuildItem;
import io.quarkus.gizmo.*;
import io.quarkus.runtime.metrics.MetricsFactory;
import io.quarkus.runtime.util.HashUtil;

public class ParametersProcessor {

    public static final DotName DN_PARAMETER = DotName.createSimple(Parameter.class);

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
    void capabilities(BuildProducer<CapabilityBuildItem> capabilityProducer) {
        capabilityProducer.produce(new CapabilityBuildItem("org.tkit.onecx.parameters", "onecx"));
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

    @BuildStep
    void validateAnnotations(CombinedIndexBuildItem combinedIndex,
            BuildProducer<ValidationPhaseBuildItem.ValidationErrorBuildItem> validationErrors) {
        List<Throwable> errors = new ArrayList<>();
        for (AnnotationInstance annotation : combinedIndex.getIndex().getAnnotations(DN_PARAMETER)) {
            AnnotationTarget target = annotation.target();
            String name = annotation.value("name").asString();
            switch (target.kind()) {
                case FIELD -> {
                    if (name == null || name.isEmpty()) {
                        ClassInfo declaringClass = target.asField().declaringClass();
                        errors.add(new EmptyAnnotationNameException(declaringClass));
                    }
                }
                case METHOD_PARAMETER -> {
                    if (name == null || name.isEmpty()) {
                        ClassInfo declaringClass = target.asMethodParameter().method().declaringClass();
                        errors.add(new EmptyAnnotationNameException(declaringClass));
                    }
                }
                default -> {
                    // No validation required for all other target kinds.
                }
            }
        }

        validationErrors.produce(new ValidationPhaseBuildItem.ValidationErrorBuildItem(errors.toArray(new Throwable[0])));
    }

    @BuildStep
    void generateProducer(CombinedIndexBuildItem combinedIndex,
            BuildProducer<GeneratedBeanBuildItem> generatedBeans) {
        IndexView index = combinedIndex.getIndex();
        Collection<AnnotationInstance> ais = index.getAnnotations(DN_PARAMETER);

        Set<DotName> names = new HashSet<>();

        for (AnnotationInstance ano : ais) {

            Type type;
            switch (ano.target().kind()) {
                case FIELD -> {
                    FieldInfo field = ano.target().asField();
                    type = field.type();
                }
                case METHOD_PARAMETER -> {
                    MethodParameterInfo methodParameter = ano.target().asMethodParameter();
                    type = methodParameter.type();
                }
                default -> {
                    /*
                     * @Parameter is allowed on methods because of the associated producer.
                     * When it is used that way, it shouldn't lead to bytecode generation in the current build step.
                     */
                    continue;
                }
            }

            ParameterizedType pt = type.asParameterizedType();
            Type vt = pt.arguments().get(0);
            names.add(vt.name());
        }

        if (!names.isEmpty()) {
            ClassOutput beansClassOutput = new GeneratedBeanGizmoAdaptor(generatedBeans);
            generateProducerClass(beansClassOutput, names);
        }
    }

    static void generateProducerClass(ClassOutput classOutput, Set<DotName> names) {
        try (ClassCreator classCreator = ClassCreator.builder().classOutput(classOutput)
                .className("org.tkit.onecx.quarkus.parameter.runtime.ParameterProducer")
                .superClass(AbstractParameterProducer.class.getName())
                .build()) {

            FieldCreator service = classCreator
                    .getFieldCreator("service", ParametersService.class.getName())
                    .setModifiers(Modifier.PUBLIC); // done to prevent warning during the build
            service.addAnnotation(Inject.class);

            for (DotName name : names) {
                String hash = "_" + HashUtil.sha1(name.toString());

                try (MethodCreator methodCreator = classCreator.getMethodCreator(
                        "produce" + name.withoutPackagePrefix() + hash, name.toString(), InjectionPoint.class.getName())) {
                    methodCreator.addAnnotation(Produces.class);
                    methodCreator.addAnnotation(Parameter.class).add("name", "ignored");

                    ResultHandle paramObj = methodCreator.readInstanceField(service.getFieldDescriptor(),
                            methodCreator.getThis());

                    ResultHandle res = methodCreator.invokeSpecialMethod(
                            MethodDescriptor.ofMethod(AbstractParameterProducer.class, "getParameter",
                                    Object.class,
                                    InjectionPoint.class, Class.class, ParametersService.class),
                            methodCreator.getThis(), methodCreator.getMethodParam(0),
                            methodCreator.loadClass(name.toString()), paramObj);

                    methodCreator.returnValue(res);
                }
            }
        }
    }
}
