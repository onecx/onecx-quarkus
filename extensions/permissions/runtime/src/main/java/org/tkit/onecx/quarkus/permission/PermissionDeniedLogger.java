package org.tkit.onecx.quarkus.permission;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Unremovable;
import io.quarkus.security.ForbiddenException;
import io.quarkus.security.spi.runtime.AuthorizationFailureEvent;

/**
 * Logs which permissions were missing when a request was rejected with 403 by the permission check.
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
        log.warn("Permission denied, missing {}", denied);
        log.debug("Granted permissions {}", checker.getGranted());
    }
}
