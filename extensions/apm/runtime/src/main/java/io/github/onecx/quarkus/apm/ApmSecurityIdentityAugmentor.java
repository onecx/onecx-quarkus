package io.github.onecx.quarkus.apm;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.security.StringPermission;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class ApmSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

    public static final String NAME = "apm";

    @Inject
    PermissionClientService service;

    @ConfigProperty(name = "onecx.apm.enabled")
    boolean enabled;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        if (!enabled) {
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity).build());
        }
        RoutingContext routingContext = (RoutingContext) identity.getAttributes().get(RoutingContext.class.getName());
        if (routingContext == null) {
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder(identity).build());
        }
        RequestContextHolder.create(routingContext.request());

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
