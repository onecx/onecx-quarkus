package org.tkit.onecx.quarkus.permission.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.quarkus.permission.PermissionRuntimeConfig;
import org.tkit.onecx.quarkus.permission.client.RequestHeaderContainer;

import gen.org.tkit.onecx.permission.api.PermissionApi;
import gen.org.tkit.onecx.permission.model.PermissionRequest;
import io.quarkus.arc.Unremovable;
import io.quarkus.cache.*;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

@Unremovable
@ApplicationScoped
public class PermissionClientService {

    @Inject
    @RestClient
    PermissionApi client;

    @CacheName("onecx-permissions")
    Cache cache;

    public Uni<PermissionResponse> getPermissions(HttpServerRequest request, PermissionRuntimeConfig config) {

        var token = request.getHeader(config.requestTokenHeaderParam());

        if (!config.cacheEnabled()) {
            return getPermissionsLocal(request, config, token);
        }
        var key = new CompositeCacheKey(config.productName(), config.applicationId(), token);
        return cache.getAsync(key,
                compositeCacheKey -> getPermissionsLocal(request, config, token))
                .onFailure().recoverWithUni(t -> cache.invalidate(key).map(x -> null));
    }

    @ActivateRequestContext
    public Uni<PermissionResponse> getPermissionsLocal(HttpServerRequest request, PermissionRuntimeConfig config,
            String token) {

        RequestHeaderContainer headerContainer = new RequestHeaderContainer();
        headerContainer.setContainerRequestContext(request.headers(), config.principalTokenHeaderParam());

        var ctx = Vertx.currentContext();
        ctx.putLocal(RequestHeaderContainer.class.getName(), headerContainer);

        return client
                .getApplicationPermissions(config.productName(), config.applicationId(), new PermissionRequest().token(token))
                .map(response -> {

                    ctx.removeLocal(RequestHeaderContainer.class.getName());

                    List<String> result = new ArrayList<>();
                    if (response.getPermissions() != null) {
                        response.getPermissions().forEach((resource, actions) -> {
                            if (actions != null && !actions.isEmpty()) {
                                actions.forEach(action -> result.add(resource + config.keySeparator() + action));
                            }
                        });
                    }
                    return PermissionResponse.create(result);
                })
                .onFailure().invoke(t -> {
                    ctx.removeLocal(RequestHeaderContainer.class.getName());
                    if (t instanceof WebApplicationException e) {
                        e.getResponse().close();
                    }
                });

    }

}
