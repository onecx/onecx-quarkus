package org.tkit.onecx.quarkus.permission;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.onecx.quarkus.permission.service.PermissionClientService;

import io.quarkus.arc.Unremovable;
import io.quarkus.security.StringPermission;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@Unremovable
@ApplicationScoped
public class OnecxSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

    @Inject
    PermissionRuntimeConfig config;

    @Inject
    PermissionClientService service;

    @Inject
    MockPermissionService mockPermissionService;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }

        if (!config.enabled()) {
            if (config.allowAll()) {
                return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity)
                        .addPermissionChecker(permission -> Uni.createFrom().item(true))
                        .build());
            }
            return Uni.createFrom().item(identity);
        }

        if (config.mock().enabled()) {
            return mockPermissionService.getMockData(identity);
        }

        RoutingContext routingContext = identity.getAttribute(RoutingContext.class.getName());
        if (routingContext == null) {
            return Uni.createFrom().item(identity);
        }

        return service
                .getPermissions(routingContext.request(), config)
                .onItem().transformToUni(response -> {
                    if (response == null) {
                        return Uni.createFrom().item(identity);
                    }
                    var actions = response.getActions();
                    if (actions == null) {
                        return Uni.createFrom().item(identity);
                    }
                    StringPermission possessedPermission = new StringPermission(config.name(), actions.toArray(new String[0]));
                    return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity)
                            .addPermissionChecker(requiredPermission -> {
                                boolean accessGranted = possessedPermission.implies(requiredPermission);
                                return Uni.createFrom().item(accessGranted);
                            })
                            .build());
                }).onFailure().recoverWithItem(identity);
    }
}
