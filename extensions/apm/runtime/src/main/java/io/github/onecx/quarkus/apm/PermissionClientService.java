package io.github.onecx.quarkus.apm;

import java.util.List;

import io.quarkus.arc.Unremovable;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@Unremovable
public interface PermissionClientService {

    void init(ApmConfig config, String applicationId);

    Uni<List<String>> getPermissions(SecurityIdentity identity);
}
