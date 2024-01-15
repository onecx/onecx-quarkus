package io.github.onecx.quarkus.permission.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;

@Path("/v1/permissions/user/application/{appId}")
@RegisterRestClient(configKey = "onecx-permission")
@RegisterClientHeaders(PermisionDefaultClientHeadersFactoryImpl.class)
public interface OnecxPermissionRestClient {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    Uni<ApplicationPermissionsDTOV1> getPermissionsForToken(@PathParam("appId") String appId, PermissionRequestDTOV1 request);

    @RegisterForReflection
    class PermissionRequestDTOV1 {
        public String token;
    }

    @RegisterForReflection
    class ApplicationPermissionsDTOV1 {

        public String appId;

        public Map<String, Set<String>> permissions = new HashMap<>();
    }
}
