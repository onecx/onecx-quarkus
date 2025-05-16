package org.tkit.onecx.quarkus.it.operator;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("onecx.tkit.org")
public class Permission extends CustomResource<PermissionSpec, PermissionStatus> implements Namespaced {
}
