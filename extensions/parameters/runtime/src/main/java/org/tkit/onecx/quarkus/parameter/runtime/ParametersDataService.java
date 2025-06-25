package org.tkit.onecx.quarkus.parameter.runtime;

import static org.tkit.onecx.quarkus.parameter.metrics.MetricsRecorder.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.parameter.TenantException;
import org.tkit.onecx.quarkus.parameter.UpdateException;
import org.tkit.onecx.quarkus.parameter.client.ClientService;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.history.ParametersHistoryEvent;
import org.tkit.onecx.quarkus.parameter.mapper.ParametersValueMapper;
import org.tkit.onecx.quarkus.parameter.metrics.MetricsRecorder;
import org.tkit.onecx.quarkus.parameter.tenant.TenantResolver;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;

import io.vertx.core.Vertx;
import io.vertx.mutiny.core.eventbus.EventBus;

@Singleton
public class ParametersDataService {

    private static final Logger log = LoggerFactory.getLogger(ParametersDataService.class);

    // tenant , tenant-parameters(key,value)
    static Map<String, TenantParameters> data = new ConcurrentHashMap<>();

    // cache data of the parameters
    static Map<String, Object> localData = new HashMap<>();

    private static final String DEFAULT_TENANT = "default-tenant";

    static Context ctxDefaultTenant = Context.builder().tenantId(DEFAULT_TENANT).build();

    @Inject
    ParametersValueMapper mapper;

    @Inject
    ClientService client;

    @Inject
    ParametersConfig config;

    @Inject
    EventBus bus;

    @Inject
    TenantResolver tenantResolver;

    @Inject
    Instance<MetricsRecorder> metricsRecorder;

    @Inject
    Vertx vertx;

    /**
     * Initialize the cache and parameters client
     *
     * @param parametersConfig the parameters management configuration
     */
    public void init(ParametersConfig parametersConfig) {

        parametersConfig.parameters().forEach((k, v) -> localData.put(k, mapper.toMap(v)));

        if (parametersConfig.cache().enabled()) {

            // update cache at start
            if (parametersConfig.cache().updateAtStart()) {
                if (parametersConfig.tenant().enabled()) {
                    log.warn("Update cache at star-up is not supported for multi-tenancy");
                } else {
                    try {
                        data.put(ctxDefaultTenant.getTenantId(), new TenantParameters(ctxDefaultTenant,
                                getParameters(ctxDefaultTenant.getTenantId(), TYPE_START_UP)));
                    } catch (UpdateException ex) {
                        log.error("Error loading parameters during start of the application. Error: {}", ex.getMessage(), ex);
                        if (parametersConfig.cache().failedAtStart()) {
                            throw ex;
                        }
                    }
                }
            }

            // setup scheduler for update
            timer();
        }
    }

    private void timer() {
        vertx.setTimer(config.cache().updateSchedule(), id -> {
            vertx.executeBlocking((Callable<Void>) () -> {
                updateTenants();
                timer();
                log.info("Update parameters cache: {}", id);
                vertx.cancelTimer(id);
                return null;
            });
        });
    }

    public void updateTenants() {
        Set<String> tenants = new HashSet<>(data.keySet());
        if (tenants.isEmpty()) {
            return;
        }
        for (var tenantId : tenants) {
            if (config.tenant().enabled()) {
                var ctx = tenantResolver.getTenantContext(data.get(tenantId).getCtx());
                ApplicationContext.start(ctx);
                try {
                    updateTenant(tenantId);
                } finally {
                    ApplicationContext.close();
                }
            } else {
                updateTenant(tenantId);
            }
        }
    }

    private void updateTenant(String tenantId) {
        try {
            var params = getParameters(tenantId, TYPE_SCHEDULER);
            data.get(tenantId).updateParameters(params);
        } catch (UpdateException ex) {
            log.warn("Update parameters for tenant: {} failed. Error: {}", tenantId, ex.getMessage());
        }
    }

    public <T> T getValue(String name, Class<T> type, T defaultValue) {
        if (!config.enabled()) {
            var raw = localData.get(name);
            if (raw == null) {
                return defaultValue;
            }
            return mapper.toType(raw, type);
        }
        var ctx = ctxDefaultTenant;
        if (config.tenant().enabled()) {
            ctx = ApplicationContext.get();
            if (ctx == null || ctx.getTenantId() == null) {
                throw new TenantException("ApplicationContext with not null tenant-id is required for multi-tenancy");
            }
        }

        var value = defaultValue;
        var raw = getRawValue(ctx, name);
        if (raw != null) {
            value = mapper.toType(raw, type);
        }
        addHistory(ctx, name, type, value, defaultValue);
        return value;
    }

    private Object getRawValue(Context ctx, String name) {
        Map<String, Object> params = new HashMap<>();
        try {
            if (config.cache().enabled()) {
                params = data
                        .computeIfAbsent(ctx.getTenantId(),
                                t -> new TenantParameters(ctx, getParameters(ctx.getTenantId(), TYPE_CACHE)))
                        .getParameters();
            } else {
                params = getParameters(ctx.getTenantId(), TYPE_NO_CACHE);
            }
        } catch (UpdateException ex) {
            log.warn("Update parameters during raw value failed. Error: {}", ex.getMessage());
            if (config.throwUpdateException()) {
                throw ex;
            }
        }

        metricsRecorder.get().increase(name);
        return params.getOrDefault(name, localData.get(name));
    }

    private <T> void addHistory(Context ctx, String name, Class<T> type, T value, T defaultValue) {
        if (!config.history().enabled()) {
            return;
        }
        bus.send(ParametersHistoryEvent.NAME, ParametersHistoryEvent.of(ctx, name, type, defaultValue, value));
    }

    public Map<String, Object> getParameters(String tenantId, String type) throws UpdateException {
        try (var response = client.getParameters(config.productName(), config.applicationId())) {
            var tmp = response.readEntity(new GenericType<HashMap<String, Object>>() {
            });
            metricsRecorder.get().update(tenantId, type, "" + response.getStatus());
            return tmp;
        } catch (Exception ex) {
            if (ex instanceof WebApplicationException w) {
                metricsRecorder.get().update(tenantId, type, "" + w.getResponse().getStatus());
                if (w.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                    return Map.of();
                }
            } else {
                metricsRecorder.get().update(tenantId, type, STATUS_UNDEFINED);
            }
            throw new UpdateException(ex);
        }
    }

}
