package org.tkit.onecx.quarkus.parameter.runtime;

import static org.tkit.onecx.quarkus.parameter.metrics.MetricsRecorder.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.GenericType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.parameter.TenantException;
import org.tkit.onecx.quarkus.parameter.UpdateException;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.history.ParametersHistoryEvent;
import org.tkit.onecx.quarkus.parameter.mapper.ParametersValueMapper;
import org.tkit.onecx.quarkus.parameter.metrics.MetricsRecorder;
import org.tkit.onecx.quarkus.parameter.tenant.TenantResolver;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.context.Context;

import gen.org.tkit.onecx.parameters.v1.api.ParameterV1Api;
import io.quarkus.arc.Arc;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.quarkus.scheduler.Scheduler;
import io.vertx.mutiny.core.eventbus.EventBus;

@ApplicationScoped
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
    @RestClient
    ParameterV1Api client;

    @Inject
    ParametersConfig config;

    @Inject
    Scheduler scheduler;

    @Inject
    EventBus bus;

    @Inject
    TenantResolver tenantResolver;

    MetricsRecorder metricsRecorder;

    private static boolean multiTenant;

    /**
     * Initialize the cache and parameters client
     *
     * @param parametersConfig the parameters management configuration
     */
    public void init(ParametersConfig parametersConfig) {
        multiTenant = parametersConfig.tenant().enabled();
        metricsRecorder = Arc.container().instance(MetricsRecorder.class).get();

        parametersConfig.parameters().forEach((k, v) -> localData.put(k, mapper.toMap(v)));

        if (parametersConfig.cache().enabled()) {

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
            scheduler.newJob("parameters-update")
                    .setCron(parametersConfig.cache().updateSchedule())
                    .setConcurrentExecution(Scheduled.ConcurrentExecution.SKIP)
                    .setTask(this::updateTenants)
                    .schedule();
        }
    }

    public void updateTenants(ScheduledExecution scheduledExecution) {

        Set<String> tenants = new HashSet<>(data.keySet());
        if (tenants.isEmpty()) {
            return;
        }
        for (var tenantId : tenants) {
            if (multiTenant) {
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
            return defaultValue;
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

        metricsRecorder.increase(name);
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
            metricsRecorder.update(tenantId, type, "" + response.getStatus());
            return tmp;
        } catch (Exception ex) {
            if (ex instanceof WebApplicationException w) {
                metricsRecorder.update(tenantId, type, "" + w.getResponse().getStatus());
            } else {
                metricsRecorder.update(tenantId, type, STATUS_UNDEFINED);
            }
            throw new UpdateException(ex);
        }
    }

}
