package io.github.onecx.quarkus.permission.client;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gen.io.github.onecx.permission.api.PermissionApi;
import gen.io.github.onecx.permission.model.PermissionRequest;
import io.quarkus.arc.Unremovable;
import io.quarkus.cache.*;
import io.smallrye.mutiny.Uni;

@Unremovable
@ApplicationScoped
public class PermissionClientService {

    private static final Logger log = LoggerFactory.getLogger(PermissionClientService.class);

    @Inject
    @RestClient
    PermissionApi client;

    @CacheName("onecx-permissions")
    Cache cache;

    public Uni<PermissionResponse> getPermissions(String appName, String token, String keySeparator, boolean cacheEnabled) {
        if (!cacheEnabled) {
            return getPermissionsLocal(appName, token, keySeparator);
        }
        var key = new CompositeCacheKey(appName, token);
        return cache.getAsync(key,
                compositeCacheKey -> getPermissionsLocal(appName, token, keySeparator))
                .onFailure().recoverWithUni(t -> cache.invalidate(key).map(x -> null));
    }

    public Uni<PermissionResponse> getPermissionsLocal(String appName, String token, String keySeparator) {

        return client.getApplicationPermissions(appName, new PermissionRequest().token(token))
                .map(response -> {
                    List<String> result = new ArrayList<>();
                    if (response.getPermissions() != null) {
                        response.getPermissions().forEach((resource, actions) -> {
                            if (actions != null && !actions.isEmpty()) {
                                actions.forEach(action -> result.add(resource + keySeparator + action));
                            }
                        });
                    }
                    return PermissionResponse.create(result);
                })
                .onFailure().invoke(t -> {
                    if (t instanceof WebApplicationException e) {
                        e.getResponse().close();
                    }
                });

    }

}
