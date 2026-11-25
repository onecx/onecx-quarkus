package org.tkit.onecx.quarkus.permission.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PermissionClientService.class);

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
                    if (log.isWarnEnabled()) {
                        log.warn("Failed to load permissions from permission service, status {}, url {}, error {}",
                                status(t), url(config), t.getClass().getName() + ": " + t.getMessage());
                    }
                    log.debug("Permission service error", t);
                    if (t instanceof WebApplicationException e) {
                        e.getResponse().close();
                    }
                });

    }

    private static Integer status(Throwable t) {
        if (t instanceof WebApplicationException e && e.getResponse() != null) {
            return e.getResponse().getStatus();
        }
        return null;
    }

    private static String url(PermissionRuntimeConfig config) {
        var restClient = PermissionApi.class.getAnnotation(RegisterRestClient.class);
        var baseUrl = ConfigProvider.getConfig()
                .getOptionalValue("quarkus.rest-client." + restClient.configKey() + ".url", String.class)
                .orElse(restClient.baseUri());
        return UriBuilder.fromUri(baseUrl).path(PermissionApi.class.getAnnotation(Path.class).value())
                .build(config.productName(), config.applicationId()).toString();
    }

}
