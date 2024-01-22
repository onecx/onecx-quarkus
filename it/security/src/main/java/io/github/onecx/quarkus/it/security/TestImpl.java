package io.github.onecx.quarkus.it.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import io.quarkus.security.PermissionsAllowed;

@ApplicationScoped
public class TestImpl implements TestInterface {

    @Override
    @PermissionsAllowed(value = "onecx:resource1#admin-write")
    public Response test1() {
        return Response.ok().build();
    }
}
