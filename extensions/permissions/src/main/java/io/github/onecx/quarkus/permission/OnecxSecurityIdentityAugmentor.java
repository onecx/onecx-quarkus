package io.github.onecx.quarkus.permission;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

import io.github.onecx.quarkus.permission.client.PermissionClientService;
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
    PermissionConfig config;

    @Inject
    RequestHeaderContainer headerContainer;

    @Inject
    PermissionClientService service;

    @Inject
    MockPermissionService mockPermissionService;

    @Override
    @ActivateRequestContext
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {

        if (!config.enabled()) {
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity)
                    .addPermissionChecker(permission -> Uni.createFrom().item(true))
                    .build());
        }

        if (config.mock().enabled()) {
            return mockPermissionService.getMockData(identity);
        }

        RoutingContext routingContext = (RoutingContext) identity.getAttributes().get(RoutingContext.class.getName());
        if (routingContext == null) {
            return Uni.createFrom().item(identity);
        }

        headerContainer.setContainerRequestContext(routingContext.request());
        var requestPermissionToken = routingContext.request().getHeader(config.permissionTokenHeaderParam());

        return service.getPermissions(config.applicationId(), requestPermissionToken)
                .onItem().transform(actions -> {
                    StringPermission possessedPermission = new StringPermission(config.name(), actions.toArray(new String[0]));
                    return QuarkusSecurityIdentity.builder(identity)
                            .addPermissionChecker(requiredPermission -> {
                                System.out.println("#CHECK# PERMISSION " + requiredPermission.getName() + "-"
                                        + requiredPermission.getActions());
                                boolean accessGranted = possessedPermission.implies(requiredPermission);
                                return Uni.createFrom().item(accessGranted);
                            })
                            .build();
                });
    }
}
