package org.tkit.onecx.quarkus.it.security;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.security.PermissionsAllowed;

@Path("test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestRestController {

    private static final Logger log = LoggerFactory.getLogger(TestRestController.class);

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Path("open")
    public Response open() {
        return Response.ok("OK").build();
    }

    @GET
    @Path("admin")
    @RolesAllowed("role-admin")
    public Response admin() {
        log.info("##  {}", httpHeaders.getRequestHeaders());
        return Response.ok("OK").build();
    }

    @GET
    @Path("write")
    @PermissionsAllowed(value = "microprofile-jwt")
    public Response adminWrite() {
        log.info("##  {}", httpHeaders.getRequestHeaders());
        return Response.ok("OK").build();
    }
}
