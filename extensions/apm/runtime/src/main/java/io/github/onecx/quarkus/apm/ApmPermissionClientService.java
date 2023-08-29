package io.github.onecx.quarkus.apm;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.onecx.quarkus.apm.client.ApmRestClient;
import io.github.onecx.quarkus.apm.client.PermissionDTOV2;
import io.github.onecx.quarkus.apm.client.PermissionDTOV3;
import io.quarkus.arc.Unremovable;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;

@Default
@Unremovable
@ApplicationScoped
public class ApmPermissionClientService implements PermissionClientService {

    private static final Logger log = LoggerFactory.getLogger(ApmPermissionClientService.class);

    ApmConfig config;

    String applicationId;

    @Inject
    @RestClient
    ApmRestClient client;

    public void init(ApmConfig config, String applicationId) {
        this.config = config;
        this.applicationId = applicationId;
    }

    @Override
    public Uni<List<String>> getPermissions(SecurityIdentity identity) {
        if (!config.enabled) {
            return Uni.createFrom().nullItem();
        }

        Uni<List<String>> actions;
        if ("v2".equals(config.apiVersion)) {
            actions = actionsV2();
        } else {
            actions = actionsV3();
        }
        if (config.log) {
            actions = actions.onItem().call(items -> {
                log.info("User {} permissions: {}", identity.getPrincipal(), items);
                return Uni.createFrom().item(items);
            });
        }
        return actions;
    }

    private Uni<List<String>> actionsV2() {

        return client
                .getPermissionsForTokenV2(applicationId)
                .map(r -> {
                    List<PermissionDTOV2> tmp = r.readEntity(new GenericType<>() {
                    });
                    return tmp.stream().map(PermissionDTOV2::getKey).toList();
                });
    }

    private Uni<List<String>> actionsV3() {

        return client
                .getPermissionsForTokenV3(applicationId)
                .map(r -> {
                    PermissionDTOV3 permissions = r.readEntity(new GenericType<>() {
                    });
                    List<String> result = new ArrayList<>();
                    permissions.forEach((resource, actions) -> {
                        if (actions != null && !actions.isEmpty()) {
                            actions.forEach(action -> result.add(resource + config.actionSeparator + action));
                        }
                    });
                    return result;
                });

    }
}
