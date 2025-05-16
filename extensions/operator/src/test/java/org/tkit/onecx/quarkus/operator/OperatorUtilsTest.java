package org.tkit.onecx.quarkus.operator;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

class OperatorUtilsTest {

    @Group("org.tkit.onecx")
    @Version("v1")
    static class TestCustomResource extends CustomResource<Object, Object> {
    }

    @Test
    void shouldProcessAdd_specIsNull_returnsFalse() {
        var resource = new TestCustomResource();
        resource.setSpec(null);
        assertFalse(OperatorUtils.shouldProcessAdd(resource));
    }

    @Test
    void shouldProcessAdd_specIsNotNull_returnsTrue() {
        var resource = new TestCustomResource();
        resource.setSpec(new Object());
        assertTrue(OperatorUtils.shouldProcessAdd(resource));
    }

    @Test
    void shouldProcessUpdate_specIsNull_returnsFalse() {
        var newResource = new TestCustomResource();
        newResource.setSpec(null);
        var oldResource = new TestCustomResource();
        assertFalse(OperatorUtils.shouldProcessUpdate(newResource, oldResource));
    }

    @Test
    void shouldProcessUpdate_metadataIsNull_returnsFalse() {
        var newResource = new TestCustomResource();
        newResource.setSpec(new Object());
        newResource.setMetadata(null);

        var oldResource = new TestCustomResource();
        oldResource.setMetadata(new ObjectMeta());

        assertFalse(OperatorUtils.shouldProcessUpdate(newResource, oldResource));
    }

    @Test
    void shouldProcessUpdate_touchAnnotationChanged_returnsTrue() {
        var newResource = new TestCustomResource();
        newResource.setSpec(new Object());
        var newMeta = new ObjectMeta();
        newMeta.setAnnotations(Map.of("org.tkit.onecx.touchedAt", "abc"));
        newResource.setMetadata(newMeta);

        var oldResource = new TestCustomResource();
        var oldMeta = new ObjectMeta();
        oldMeta.setAnnotations(Map.of("org.tkit.onecx.touchedAt", "xyz"));
        oldResource.setMetadata(oldMeta);

        assertTrue(OperatorUtils.shouldProcessUpdate(newResource, oldResource));
    }

    @Test
    void shouldProcessUpdate_generationChanged_returnsTrue() {
        var newResource = new TestCustomResource();
        newResource.setSpec(new Object());
        var newMeta = new ObjectMeta();
        newMeta.setAnnotations(Map.of("org.tkit.onecx.touchedAt", "same"));
        newMeta.setGeneration(2L);
        newResource.setMetadata(newMeta);

        var oldResource = new TestCustomResource();
        var oldMeta = new ObjectMeta();
        oldMeta.setAnnotations(Map.of("org.tkit.onecx.touchedAt", "same"));
        oldMeta.setGeneration(1L);
        oldResource.setMetadata(oldMeta);

        assertTrue(OperatorUtils.shouldProcessUpdate(newResource, oldResource));
    }

    @Test
    void shouldProcessUpdate_nothingChanged_returnsFalse() {
        var newResource = new TestCustomResource();
        newResource.setSpec(new Object());
        var newMeta = new ObjectMeta();
        newMeta.setAnnotations(Map.of("org.tkit.onecx.touchedAt", "same"));
        newMeta.setGeneration(1L);
        newResource.setMetadata(newMeta);

        var oldResource = new TestCustomResource();
        var oldMeta = new ObjectMeta();
        oldMeta.setAnnotations(Map.of("org.tkit.onecx.touchedAt", "same"));
        oldMeta.setGeneration(1L);
        oldResource.setMetadata(oldMeta);

        assertFalse(OperatorUtils.shouldProcessUpdate(newResource, oldResource));
    }
}
