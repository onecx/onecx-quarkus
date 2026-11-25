package org.tkit.onecx.quarkus.permission;

import java.security.Permission;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import io.quarkus.security.StringPermission;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

/**
 * Request scoped permission checker. It remembers denied permissions so that
 * {@link PermissionDeniedLogger} can log them for a 403 response.
 */
public class OnecxPermissionChecker implements Function<Permission, Uni<Boolean>> {

    public static final String ATTRIBUTE = OnecxPermissionChecker.class.getName();

    private final List<String> granted;

    private final StringPermission possessed;

    // created on first deny only, so granted requests do not pay for it
    private Set<String> denied;

    private OnecxPermissionChecker(String name, List<String> granted) {
        this.granted = granted;
        this.possessed = new StringPermission(name, granted.toArray(new String[0]));
    }

    public static OnecxPermissionChecker of(String name, List<String> granted) {
        return new OnecxPermissionChecker(name, granted);
    }

    @Override
    public Uni<Boolean> apply(Permission requiredPermission) {
        boolean accessGranted = possessed.implies(requiredPermission);
        if (!accessGranted) {
            addDenied(format(requiredPermission));
        }
        return Uni.createFrom().item(accessGranted);
    }

    private synchronized void addDenied(String permission) {
        if (denied == null) {
            denied = new LinkedHashSet<>();
        }
        denied.add(permission);
    }

    public SecurityIdentity augment(SecurityIdentity identity) {
        return QuarkusSecurityIdentity.builder(identity)
                .addPermissionChecker(this)
                .addAttribute(ATTRIBUTE, this)
                .build();
    }

    public List<String> getGranted() {
        return granted;
    }

    public synchronized Set<String> getDenied() {
        return denied == null ? Set.of() : new LinkedHashSet<>(denied);
    }

    static String format(Permission permission) {
        var actions = permission.getActions();
        if (actions == null || actions.isEmpty()) {
            return permission.getName();
        }
        return permission.getName() + ":" + actions;
    }
}
