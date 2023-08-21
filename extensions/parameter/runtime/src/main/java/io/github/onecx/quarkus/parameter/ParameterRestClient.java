package io.github.onecx.quarkus.parameter;

import java.util.Map;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@Path("/v3")
@RegisterRestClient(configKey = "quarkus-param-config-rest-client")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ParameterRestClient {

    @GET
    @Path("{app}/parameters")
    Uni<Map<String, String>> getParameters(@PathParam("app") String app);

    @POST
    @Path("{app}/history")
    Uni<Response> sendMetrics(@PathParam("app") String app, ParametersBucket bucket);
}
