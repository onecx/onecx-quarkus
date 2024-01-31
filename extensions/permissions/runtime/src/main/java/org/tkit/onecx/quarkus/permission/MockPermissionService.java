package org.tkit.onecx.quarkus.permission;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.security.StringPermission;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class MockPermissionService {

    @Inject
    PermissionRuntimeConfig config;

    public Uni<SecurityIdentity> getMockData(SecurityIdentity identity) {
        if (identity.getPrincipal() == null) {
            return Uni.createFrom().item(identity);
        }

        var roles = identity.getRoles();
        if (roles == null || roles.isEmpty()) {
            return Uni.createFrom().item(identity);
        }

        List<String> mockData = new ArrayList<>();
        roles.forEach(role -> {
            var data = config.mock.roles.get(role);
            if (data != null) {
                data.forEach((resource, actions) -> {
                    mockData.addAll(actions.stream().map(action -> resource + config.keySeparator + action).toList());
                });
            }
        });

        StringPermission mockPermissions = new StringPermission(config.name, mockData.toArray(new String[0]));
        return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity)
                .addPermissionChecker(requiredPermission -> {
                    boolean accessGranted = mockPermissions.implies(requiredPermission);
                    return Uni.createFrom().item(accessGranted);
                })
                .build());

    }
}
