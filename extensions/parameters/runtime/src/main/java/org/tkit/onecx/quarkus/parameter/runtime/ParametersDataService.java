package org.tkit.onecx.quarkus.parameter.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.onecx.quarkus.parameter.client.ParameterClientService;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.mapper.ParametersValueMapper;
import org.tkit.onecx.quarkus.parameter.tenant.ParametersTenantResolver;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;

import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
public class ParametersDataService {

    // tenant , parameters(key,value)
    static Map<String, Map<String, Object>> data = new ConcurrentHashMap<>();

    // cache data of the parameters
    static Map<String, Object> localData = new HashMap<>();

    @Inject
    ParametersValueMapper mapper;

    @Inject
    ParameterClientService clientService;

    @Inject
    ParametersTenantResolver tenantResolver;

    @Inject
    Vertx vertx;

    @Inject
    ParametersConfig config;

    /**
     * Initialize the cache and parameters client
     *
     * @param parametersConfig the parameters management configuration
     */
    public void init(ParametersConfig parametersConfig) {
        parametersConfig.parameters().forEach((k, v) -> localData.put(k, mapper.toMap(v)));
        if (parametersConfig.cache().enabled()) {
            if (parametersConfig.cache().updateAtStart()) {
                try {
                    var params = clientService.getParameters();
                    data.put(tenantResolver.defaultTenant(), params);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // setup scheduler for update
            vertx.setPeriodic(parametersConfig.cache().updateIntervalInMilliseconds(), this::updateTenants);
        }
    }

    public void updateTenants(Long id) {
        if (config.tenant().enabled()) {
            Set<String> tenants = new HashSet<>(data.keySet());
            if (tenants.isEmpty()) {
                return;
            }
            for (var tenantId : tenants) {
                var ctx = Context.builder()
                        .tenantId(tenantId)
                        .build();
                ApplicationContext.start(ctx);

                try {
                    updateTenant(tenantId);
                } finally {
                    ApplicationContext.close();
                }
            }
        } else {
            updateTenant(tenantResolver.defaultTenant());
        }
    }

    private void updateTenant(String tenantId) {
        try {
            var params = clientService.getParameters();
            data.put(tenantId, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Object getRawValue(String name) {

        var tenantId = tenantResolver.getTenant();

        Map<String, Object> params;
        if (config.cache().enabled()) {
            params = data.computeIfAbsent(tenantId, x -> clientService.getParameters());
        } else {
            params = clientService.getParameters();
        }

        var raw = params.get(name);
        if (raw != null) {
            return raw;
        }

        // none tenant relevant
        return localData.get(name);
    }

}
