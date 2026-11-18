package org.tkit.onecx.quarkus.permission;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.permission.service.PermissionClientService;

import io.quarkus.arc.Unremovable;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@Unremovable
@ApplicationScoped
public class OnecxSecurityIdentityAugmentor implements SecurityIdentityAugmentor {

    private static final Logger log = LoggerFactory.getLogger(OnecxSecurityIdentityAugmentor.class);

    @Inject
    PermissionRuntimeConfig config;

    @Inject
    PermissionClientService service;

    @Inject
    MockPermissionService mockPermissionService;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        return Uni.createFrom().item(identity);
    }

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context,
            Map<String, Object> attributes) {
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

        RoutingContext routingContext = HttpSecurityUtils.getRoutingContextAttribute(attributes);
        if (routingContext == null) {
            return Uni.createFrom().item(identity);
        }

        return service
                .getPermissions(routingContext.request(), config)
                .onItem().transform(response -> {
                    if (response == null || response.getActions() == null) {
                        return OnecxPermissionChecker.notLoaded().augment(identity);
                    }
                    return OnecxPermissionChecker
                            .of(config.name(), response.getActions())
                            .augment(identity);
                }).onFailure().recoverWithItem(t -> {
                    log.debug("Permission resolution failed, all permission checks will be denied", t);
                    return OnecxPermissionChecker.notLoaded().augment(identity);
                });
    }
}
