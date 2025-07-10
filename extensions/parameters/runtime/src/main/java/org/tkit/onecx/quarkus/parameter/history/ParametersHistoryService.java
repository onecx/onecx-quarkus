package org.tkit.onecx.quarkus.parameter.history;

import java.util.concurrent.Callable;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.parameter.client.ClientService;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.metrics.MetricsRecorder;
import org.tkit.onecx.quarkus.parameter.tenant.TenantResolver;
import org.tkit.quarkus.context.ApplicationContext;

import io.quarkus.arc.Arc;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.Vertx;

@Singleton
public class ParametersHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ParametersHistoryService.class);

    @Inject
    ClientService client;

    @Inject
    ParametersConfig config;

    @Inject
    TenantResolver resolver;

    @Inject
    Vertx vertx;

    MetricsRecorder metricsRecorder;

    // The history contains the current collected requests
    private ParametersHistory history;

    private static String instanceId;

    private static boolean multiTenant;

    public void init(ParametersConfig parametersConfig) {
        instanceId = parametersConfig.instanceId().orElse(null);
        history = new ParametersHistory(instanceId);

        if (!parametersConfig.history().enabled()) {
            return;
        }

        multiTenant = parametersConfig.tenant().enabled();
        metricsRecorder = Arc.container().instance(MetricsRecorder.class).get();

        // update
        timer();
    }

    private void timer() {
        vertx.setTimer(config.history().updateSchedule(), id -> {
            vertx.executeBlocking((Callable<Void>) () -> {
                sendHistory();
                timer();
                log.info("Update history: {}", id);
                vertx.cancelTimer(id);
                return null;
            });
        });
    }

    private void sendHistory() {
        ParametersHistory tmp = this.history;
        this.history = new ParametersHistory(instanceId);
        tmp.end();

        // do not send empty bucket
        if (tmp.isEmpty()) {
            return;
        }

        tmp.getTenants().forEach((tenantId, value) -> {
            if (value.isEmpty()) {
                return;
            }
            if (multiTenant) {
                var ctx = resolver.getTenantContext(value.getCtx());
                ApplicationContext.start(ctx);
                try {
                    sendMetrics(tenantId, history, value);
                } finally {
                    ApplicationContext.close();
                }
            } else {
                sendMetrics(value.getCtx().getTenantId(), history, value);
            }
        });
    }

    private void sendMetrics(String tenantId, ParametersHistory history, ParametersHistory.TenantParameters parameters) {

        try (var response = client.bucketRequest(config.productName(), config.applicationId(), history, parameters)) {
            if (response.getStatus() != Response.Status.OK.getStatusCode()
                    && response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
                log.error("Error send metrics to the parameters management. Code: {}", response.getStatus());
            }
            log.debug("Send metrics. Code: {}", response.getStatus());
            metricsRecorder.history(tenantId, "" + response.getStatus());
        } catch (Exception ex) {
            if (ex instanceof WebApplicationException w) {
                metricsRecorder.history(tenantId, "" + w.getResponse().getStatus());
            } else {
                metricsRecorder.history(tenantId, MetricsRecorder.STATUS_UNDEFINED);
            }
            log.error("Error send metrics to the parameters management. Error: {}", ex.getMessage());
        }
    }

    @ConsumeEvent(ParametersHistoryEvent.NAME)
    public void consumeEvent(ParametersHistoryEvent event) {
        history.addParameterRequest(event);
    }

}
