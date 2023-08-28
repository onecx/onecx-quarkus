package io.github.onecx.quarkus.apm.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.smallrye.mutiny.Uni;

@Path("/")
@RegisterClientHeaders
@RegisterRestClient(configKey = "onecx-apm")
public interface ApmRestClient {
    @GET
    @Path("/v2/applications/{applicationNamespaceId}/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> getPermissionsForTokenV2(
            @PathParam("applicationNamespaceId") String applicationNamespaceId,
            @HeaderParam("apm-principal-roles") String apmPrincipalRoles,
            @HeaderParam("apm-principal-token") String apmPrincipalToken);

    @GET
    @Path("/v3/applications/{applicationNamespaceId}/permissions")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<Response> getPermissionsForTokenV3(
            @PathParam("applicationNamespaceId") String applicationNamespaceId);
}
