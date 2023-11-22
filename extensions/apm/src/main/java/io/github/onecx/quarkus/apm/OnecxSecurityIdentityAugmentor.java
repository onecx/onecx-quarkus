package io.github.onecx.quarkus.apm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

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
    ApmConfig config;

    @Inject
    ApmHeaderContainer headerContainer;

    @Inject
    PermissionClientService service;

    @Override
    @ActivateRequestContext
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {

        if (!config.enabled()) {
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity).build());
        }

        RoutingContext routingContext = (RoutingContext) identity.getAttributes().get(RoutingContext.class.getName());
        if (routingContext == null) {
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity).build());
        }

        headerContainer.setContainerRequestContext(routingContext.request());

        return service.getPermissions(identity)
                .onItem().transform(actions -> {
                    StringPermission possessedPermission = new StringPermission(config.name(), actions.toArray(new String[0]));
                    return QuarkusSecurityIdentity.builder(identity)
                            .addPermissionChecker(requiredPermission -> {
                                boolean accessGranted = possessedPermission.implies(requiredPermission);
                                return Uni.createFrom().item(accessGranted);
                            })
                            .build();
                });

    }

}
