package io.github.onecx.quarkus.apm;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import io.vertx.core.http.HttpServerRequest;

public class RequestContext {

    MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

    public RequestContext(HttpServerRequest request) {
        request.headers().names().forEach(name -> {
            var value = request.headers().getAll(name);
            if (value != null && !value.isEmpty()) {
                headers.put(name, value);
            }

        });
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

}
