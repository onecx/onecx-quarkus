package io.github.onecx.quarkus.apm;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import io.quarkus.arc.Unremovable;
import io.vertx.core.http.HttpServerRequest;

@RequestScoped
@Unremovable
public class ApmHeaderContainer {

    private static final MultivaluedHashMap<String, String> EMPTY_MAP = new MultivaluedHashMap<>();
    private HttpServerRequest requestContext;

    void setContainerRequestContext(HttpServerRequest requestContext) {
        this.requestContext = requestContext;
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
