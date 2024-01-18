package io.github.onecx.quarkus.permission.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;

@Path("/v1/permissions/user/application/{appId}")
@RegisterRestClient(configKey = "onecx_permission")
@RegisterClientHeaders(PermissionDefaultClientHeadersFactoryImpl.class)
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

        public Map<String, List<String>> permissions = new HashMap<>();
    }
}
