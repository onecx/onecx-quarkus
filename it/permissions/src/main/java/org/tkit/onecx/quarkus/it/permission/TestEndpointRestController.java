package org.tkit.onecx.quarkus.it.permission;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("headers/test/endpoint")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class TestEndpointRestController {

    @RestClient
    @Inject
    TestClient client;

    @GET
    @Path("{name}")
    public Response get(@PathParam("name") String name) {
        try (Response r = client.get(name)) {
            return Response.ok(r.readEntity(String.class)).build();
        }
    }
}
