package io.github.onecx.quarkus.apm.client;

import java.util.HashMap;
import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;

@Path("/")
@RegisterRestClient(configKey = "onecx-apm-svc")
@RegisterClientHeaders(ApmDefaultClientHeadersFactory.class)
public interface ApmRestClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{version}/applications/{applicationNamespaceId}/permissions")
    Uni<Response> getPermissionsForToken(
            @PathParam("version") String version,
            @PathParam("applicationNamespaceId") String applicationNamespaceId);

    @RegisterForReflection
    class PermissionDTOV3 extends HashMap<String, List<String>> {

    }
}
