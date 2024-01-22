package io.github.onecx.quarkus.tenant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gen.io.github.onecx.tenant.client.api.TenantV1Api;
import gen.io.github.onecx.tenant.client.model.TenantId;
import io.quarkus.arc.Unremovable;
import io.quarkus.cache.CacheResult;

@Unremovable
@RequestScoped
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);

    @Inject
    @RestClient
    TenantV1Api client;

    @Inject
    TenantConfig config;

    public String getTenant(String token) {
        if (config.cacheEnabled()) {
            return getTenantCache(token);
        }
        return getTenantLocal(token);
    }

    @CacheResult(cacheName = "onecx-tenant")
    public String getTenantCache(String token) {
        return getTenantLocal(token);
    }

    public String getTenantLocal(String token) {

        try (Response response = client.getTenantMapsByOrgId()) {
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                TenantId data = response.readEntity(TenantId.class);
                return data.getTenantId();
            }

            var body = response.readEntity(String.class);
            log.debug("Wrong response to get tenant-id from service. Status: {}, Entity: {}", response.getStatus(), body);

            throw new TenantException(ErrorKeys.ERROR_TENANT_SERVICE_WRONG_RESPONSE,
                    "Wrong response to get tenant-id from service. Status: " + response.getStatus());

        } catch (WebApplicationException ex) {
            var body = ex.getResponse().readEntity(String.class);
            log.error("Error get tenant-id from service. Status: {}, Message: {}", ex.getResponse().getStatus(), body);

            throw new TenantException(ErrorKeys.ERROR_TENANT_SERVICE_EXCEPTION_RESPONSE,
                    "Error get tenant-id from service. Status: " + ex.getResponse().getStatus());
        }
    }

    public enum ErrorKeys {

        ERROR_TENANT_SERVICE_WRONG_RESPONSE,
        ERROR_TENANT_SERVICE_EXCEPTION_RESPONSE;
    }
}
