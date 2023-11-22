package io.github.onecx.quarkus.tenant.client;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RequestScoped
@Path("/v1/tenant")
@RegisterClientHeaders(TenantDefaultClientHeadersFactoryImpl.class)
@RegisterRestClient(configKey = "onecx-tenant-svc")
public interface OnecxTenantClientRestClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Response getTenantId();

}
