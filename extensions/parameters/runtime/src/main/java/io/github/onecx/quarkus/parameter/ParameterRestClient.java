package io.github.onecx.quarkus.parameter;

import java.util.Map;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.smallrye.mutiny.Uni;

@Path("/v3")
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
