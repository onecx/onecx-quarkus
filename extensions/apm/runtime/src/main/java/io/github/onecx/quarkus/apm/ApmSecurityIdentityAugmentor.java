package io.github.onecx.quarkus.apm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.security.StringPermission;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ApmSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

    public static final String NAME = "apm";

    @Inject
    PermissionClientService service;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        return service.getPermissions(identity)
                .onItem().transform(actions -> {
                    StringPermission possessedPermission = new StringPermission(NAME, actions.toArray(new String[0]));
                    return QuarkusSecurityIdentity.builder(identity)
                            .addPermissionChecker(requiredPermission -> {
                                boolean accessGranted = possessedPermission.implies(requiredPermission);
                                return Uni.createFrom().item(accessGranted);
                            })
                            .build();
                });
    }

}
