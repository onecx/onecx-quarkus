package org.tkit.onecx.quarkus.permission;

import java.security.Permission;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.quarkus.security.StringPermission;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;

/**
 * Request scoped OneCX permission checker. It remembers denied permissions so that
 * {@link PermissionDeniedLogger} can explain a 403 response.
 */
public class OnecxPermissionChecker implements Function<Permission, Uni<Boolean>> {

    public static final String ATTRIBUTE = OnecxPermissionChecker.class.getName();

    private final List<String> granted;

    private final StringPermission possessed;

    private final Set<String> denied = ConcurrentHashMap.newKeySet();

    private OnecxPermissionChecker(String name, List<String> granted) {
        this.granted = granted == null ? List.of() : List.copyOf(granted);
        this.possessed = name == null ? null : new StringPermission(name, this.granted.toArray(new String[0]));
    }

    public static OnecxPermissionChecker of(String name, List<String> granted) {
        return new OnecxPermissionChecker(name, granted);
    }

    public static OnecxPermissionChecker notLoaded() {
        return new OnecxPermissionChecker(null, null);
    }

    @Override
    public Uni<Boolean> apply(Permission requiredPermission) {
        boolean accessGranted = possessed != null && possessed.implies(requiredPermission);
        if (!accessGranted) {
            denied.add(format(requiredPermission));
        }
        return Uni.createFrom().item(accessGranted);
    }

    public SecurityIdentity augment(SecurityIdentity identity) {
        return QuarkusSecurityIdentity.builder(identity)
                .addPermissionChecker(this)
                .addAttribute(ATTRIBUTE, this)
                .build();
    }

    public boolean isLoaded() {
        return possessed != null;
    }

    public List<String> getGranted() {
        return granted;
    }

    public Set<String> getDenied() {
        return Set.copyOf(denied);
    }

    static String format(Permission permission) {
        var actions = permission.getActions();
        if (actions == null || actions.isEmpty()) {
            return permission.getName();
        }
        return permission.getName() + ":" + actions;
    }
}
