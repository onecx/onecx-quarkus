package org.tkit.onecx.quarkus.operator;

import java.util.Objects;

import org.eclipse.microprofile.config.ConfigProvider;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class OperatorUtils {

    static public <S, T> boolean shouldProcessAdd(CustomResource<S, T> resource) {
        return resource.getSpec() != null;
    }

    static public <S, T> boolean shouldProcessUpdate(CustomResource<S, T> newResource, CustomResource<S, T> oldResource) {
        if (newResource.getSpec() == null) {
            return false;
        }
        if (newResource.getMetadata() == null || oldResource.getMetadata() == null) {
            return false;
        }
        if (touchAnnotationChanged(newResource.getMetadata(), oldResource.getMetadata())) {
            return true;
        }

        return !Objects.equals(newResource.getMetadata().getGeneration(),
                oldResource.getMetadata().getGeneration());
    }

    static private boolean touchAnnotationChanged(ObjectMeta newResource, ObjectMeta oldResource) {
        var touchAnnotation = ConfigProvider.getConfig().getValue("onecx.operator.touch-annotation", String.class);
        return !Objects.equals(getTouchAnnotation(newResource, touchAnnotation),
                getTouchAnnotation(oldResource, touchAnnotation));
    }

    static private String getTouchAnnotation(ObjectMeta resource, String annotation) {
        if (resource.getAnnotations() == null) {
            return null;
        }
        return resource.getAnnotations().get(annotation);
    }

}
