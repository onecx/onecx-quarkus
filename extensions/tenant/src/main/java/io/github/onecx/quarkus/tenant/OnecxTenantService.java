package io.github.onecx.quarkus.tenant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.onecx.quarkus.tenant.client.OnecxTenantClientRestClient;
import io.quarkus.arc.Unremovable;
import io.quarkus.cache.CacheResult;

@Unremovable
@RequestScoped
public class OnecxTenantService {

    private static final Logger log = LoggerFactory.getLogger(OnecxTenantService.class);

    @Inject
    @RestClient
    OnecxTenantClientRestClient client;

    @SuppressWarnings("java:S2139")
    @CacheResult(cacheName = "onecx-tenant")
    public String getTenant(String token) {

        try (Response response = client.getTenantId()) {
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                TenantIdDTOV1 data = response.readEntity(TenantIdDTOV1.class);
                return data.getTenantId();
            }

            var body = response.readEntity(String.class);
            log.error("Wrong response to get tenant-id from service. Status: {}, Entity: {}", response.getStatus(), body);

            throw new OnecxTenantException(ErrorKeys.ERROR_TENANT_SERVICE_WRONG_RESPONSE,
                    "Wrong response to get tenant-id from service. Status: " + response.getStatus() + ", Message: " + body);

        } catch (WebApplicationException ex) {
            var body = ex.getResponse().readEntity(String.class);
            log.error("Error get tenant-id from service. Status: {}, Message: {}", ex.getResponse().getStatus(), body, ex);

            throw new OnecxTenantException(ErrorKeys.ERROR_TENANT_SERVICE_EXCEPTION_RESPONSE,
                    "Error get tenant-id from service. Status: " + ex.getResponse().getStatus() + ", Message: " + body,
                    ex);
        }
    }

    public static class TenantIdDTOV1 {
        private String tenantId;

        public String getTenantId() {
            return tenantId;
        }

    }

    public enum ErrorKeys {

        ERROR_TENANT_SERVICE_WRONG_RESPONSE,
        ERROR_TENANT_SERVICE_EXCEPTION_RESPONSE;
    }
}
