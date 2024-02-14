package org.tkit.onecx.quarkus.it.permission;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("headers/test/client")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class TestClientRestController {

    private static final Logger log = LoggerFactory.getLogger(TestClientRestController.class);

    @Context
    HttpHeaders httpHeaders;

    @GET
    @Path("{name}")
    public Response get(@PathParam("name") String name) {
        var tmp = httpHeaders.getHeaderString(name);
        log.info("Header name '{}' value '{}'", name, tmp);
        return Response.ok(tmp).build();
    }
}
