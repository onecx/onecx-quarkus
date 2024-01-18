package io.github.onecx.quarkus.permission;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import io.quarkus.arc.Unremovable;
import io.vertx.core.http.HttpServerRequest;

@RequestScoped
@Unremovable
public class RequestHeaderContainer {

    private static final MultivaluedHashMap<String, String> EMPTY_MAP = new MultivaluedHashMap<>();
    private HttpServerRequest requestContext;

    private String tokenHeaderParam;

    void setContainerRequestContext(HttpServerRequest requestContext, String tokenHeaderParam) {
        this.requestContext = requestContext;
        this.tokenHeaderParam = tokenHeaderParam;
    }

    public String getTokenHeaderParam() {
        return tokenHeaderParam;
    }

    public MultivaluedMap<String, String> getHeaders() {
        if (requestContext == null) {
            return EMPTY_MAP;
        } else {

            MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

            requestContext.headers().names().forEach(name -> {
                var value = requestContext.headers().getAll(name);
                if (value != null && !value.isEmpty()) {
                    headers.put(name, value);
                }

            });
            return headers;
        }
    }
}
