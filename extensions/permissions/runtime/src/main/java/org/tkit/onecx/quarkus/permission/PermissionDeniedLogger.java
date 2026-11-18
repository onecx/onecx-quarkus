package org.tkit.onecx.quarkus.permission;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Unremovable;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.spi.runtime.AuthorizationFailureEvent;
import io.vertx.ext.web.RoutingContext;

/**
 * Logs why a request was rejected with 403 by the OneCX permission check.
 */
@Unremovable
@ApplicationScoped
public class PermissionDeniedLogger {

    private static final Logger log = LoggerFactory.getLogger(PermissionDeniedLogger.class);

    void onAuthorizationFailure(@Observes AuthorizationFailureEvent event) {
        if (!log.isWarnEnabled() || !(event.getAuthorizationFailure() instanceof ForbiddenException)) {
            return;
        }
        var identity = event.getSecurityIdentity();
        if (identity == null
                || !(identity.getAttribute(OnecxPermissionChecker.ATTRIBUTE) instanceof OnecxPermissionChecker checker)) {
            return;
        }
        var denied = checker.getDenied();
        if (denied.isEmpty()) {
            return;
        }

        var sb = new StringBuilder("OneCX permission denied");
        if (event.getEventProperties() != null
                && event.getEventProperties().get(RoutingContext.class.getName()) instanceof RoutingContext rc) {
            sb.append(", request ").append(rc.request().method().name()).append(' ').append(rc.request().path());
        }
        sb.append(", required ").append(denied);
        if (checker.isLoaded()) {
            sb.append(", granted ").append(checker.getGranted().size()).append(" permission(s)");
        } else {
            sb.append(", permissions could not be loaded from onecx-permission-svc");
        }
        log.warn(sb.toString());

        if (checker.isLoaded()) {
            log.debug("OneCX permissions granted: {}", checker.getGranted());
        }
    }
}
