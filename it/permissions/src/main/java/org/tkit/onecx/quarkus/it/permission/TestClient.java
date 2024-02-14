package org.tkit.onecx.quarkus.it.permission;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("headers/test/client")
@RegisterRestClient(baseUri = "http://localhost:8081", configKey = "test_client")
@RegisterClientHeaders
public interface TestClient {

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    Response get(@PathParam("name") String name);
}
