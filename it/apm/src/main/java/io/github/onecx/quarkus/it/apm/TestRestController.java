package io.github.onecx.quarkus.it.apm;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.security.PermissionsAllowed;

@Path("test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestRestController {

    @GET
    @Path("open")
    public Response open() {
        return Response.ok("OK").build();
    }

    @GET
    @Path("admin")
    @RolesAllowed("role-admin")
    public Response admin() {
        return Response.ok("OK").build();
    }

    @GET
    @Path("write")
    @PermissionsAllowed(value = "apm:resource1#admin-write")
    public Response adminWrite() {
        return Response.ok("OK").build();
    }
}
