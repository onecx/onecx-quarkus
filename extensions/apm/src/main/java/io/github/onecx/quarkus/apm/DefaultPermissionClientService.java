package io.github.onecx.quarkus.apm;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.onecx.quarkus.apm.client.ApmRestClient;
import io.github.onecx.quarkus.apm.client.PermissionDTOV2;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@DefaultBean
@Unremovable
@ApplicationScoped
public class DefaultPermissionClientService implements PermissionClientService {

    private static final Logger log = LoggerFactory.getLogger(DefaultPermissionClientService.class);

    @Inject
    ApmConfig config;

    @Inject
    @RestClient
    ApmRestClient client;

    @Override
    public Uni<List<String>> getPermissions(SecurityIdentity identity) throws OnecxApmErrorException {

        Uni<List<String>> actions = client
                .getPermissionsForToken(config.clientVersion(), config.applicationId())
                .map(this::response);

        if (config.debugLog()) {

            actions = actions.onItem().call(items -> {
                log.info("User {} permissions: {}", identity.getPrincipal(), items);
                return Uni.createFrom().item(items);
            });
        }
        return actions;
    }

    private List<String> response(Response response) {
        if ("v3".equals(config.clientVersion())) {
            var permissions = response.readEntity(new GenericType<ApmRestClient.PermissionDTOV3>() {
            });

            List<String> result = new ArrayList<>();
            permissions.forEach((resource, actions) -> {
                if (actions != null && !actions.isEmpty()) {
                    actions.forEach(action -> result.add(resource + config.clientV3().separator() + action));
                }
            });
            return result;
        }

        if ("v2".equals(config.clientVersion())) {
            return response.readEntity(new GenericType<List<PermissionDTOV2>>() {
            }).stream().map(PermissionDTOV2::getKey).toList();
        }

        throw new OnecxApmErrorException(ErrorKeys.ERROR_NOT_SUPPORTED_API_VERSION,
                "Not supported version client version. Version: " + config.clientVersion());
    }

    public enum ErrorKeys {

        ERROR_NOT_SUPPORTED_API_VERSION;
    }

}
