package org.tkit.onecx.quarkus.permission.client;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.reactive.common.util.CaseInsensitiveMap;

import io.vertx.core.MultiMap;

public class RequestHeaderContainer {

    private MultivaluedMap<String, String> incomingHeaders = new MultivaluedHashMap<>();

    private String tokenHeaderParam;

    public void setContainerRequestContext(MultiMap headers, String tokenHeaderParam) {
        this.tokenHeaderParam = tokenHeaderParam;
        if (headers == null || headers.isEmpty()) {
            return;
        }
        incomingHeaders = new CaseInsensitiveMap<>();
        headers.names().forEach(name -> {
            var value = headers.getAll(name);
            if (value != null && !value.isEmpty()) {
                incomingHeaders.put(name, value);
            }

        });
    }

    public String getTokenHeaderParam() {
        return tokenHeaderParam;
    }

    public MultivaluedMap<String, String> getIncomingHeaders() {
        return incomingHeaders;
    }
}
