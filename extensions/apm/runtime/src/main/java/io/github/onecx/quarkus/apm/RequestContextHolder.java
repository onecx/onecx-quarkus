package io.github.onecx.quarkus.apm;

import io.vertx.core.http.HttpServerRequest;

public class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    static RequestContext close() {
        RequestContext tmp = CONTEXT.get();
        CONTEXT.remove();
        return tmp;
    }

    static void create(HttpServerRequest request) {
        CONTEXT.set(new RequestContext(request));
    }
}
