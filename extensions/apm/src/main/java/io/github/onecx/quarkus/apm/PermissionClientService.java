package io.github.onecx.quarkus.apm;

import java.util.List;

import io.quarkus.arc.Unremovable;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@Unremovable
public interface PermissionClientService {

    Uni<List<String>> getPermissions(SecurityIdentity identity) throws OnecxApmErrorException;
}
