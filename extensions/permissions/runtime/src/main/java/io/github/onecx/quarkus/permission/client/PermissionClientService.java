package io.github.onecx.quarkus.permission.client;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.github.onecx.quarkus.permission.PermissionRuntimeConfig;
import io.quarkus.arc.Unremovable;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;

@Unremovable
@ApplicationScoped
public class PermissionClientService {

    @Inject
    @RestClient
    OnecxPermissionRestClient client;

    @Inject
    PermissionRuntimeConfig config;

    @CacheResult(cacheName = "onecx-permissions")
    public Uni<List<String>> getPermissions(String appName, String token) {
        var request = new OnecxPermissionRestClient.PermissionRequestDTOV1();
        request.token = token;
        return client.getPermissionsForToken(appName, request)
                .map(response -> {
                    List<String> result = new ArrayList<>();
                    response.permissions.forEach((resource, actions) -> {
                        if (actions != null && !actions.isEmpty()) {
                            actions.forEach(action -> result.add(resource + config.keySeparator + action));
                        }
                    });

                    return result;
                });
    };
}
