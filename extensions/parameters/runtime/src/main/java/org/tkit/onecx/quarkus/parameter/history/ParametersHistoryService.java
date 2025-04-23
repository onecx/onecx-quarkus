package org.tkit.onecx.quarkus.parameter.history;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.metrics.MetricsRecorder;
import org.tkit.onecx.quarkus.parameter.tenant.TenantResolver;
import org.tkit.quarkus.context.ApplicationContext;

import gen.org.tkit.onecx.parameters.v1.api.ParameterV1Api;
import gen.org.tkit.onecx.parameters.v1.model.ParameterInfo;
import gen.org.tkit.onecx.parameters.v1.model.ParametersBucket;
import io.quarkus.arc.Arc;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import io.quarkus.scheduler.Scheduler;
import io.quarkus.vertx.ConsumeEvent;

@Singleton
public class ParametersHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ParametersHistoryService.class);

    @Inject
    Scheduler scheduler;

    @Inject
    @RestClient
    ParameterV1Api client;

    @Inject
    ParametersConfig config;

    @Inject
    TenantResolver resolver;

    MetricsRecorder metricsRecorder;

    // The history contains the current collected requests
    private ParametersHistory history;

    private static String instanceId;

    private static boolean multiTenant;

    public void init(ParametersConfig parametersConfig) {
        metricsRecorder = Arc.container().instance(MetricsRecorder.class).get();

        // init rest client
        instanceId = parametersConfig.instanceId().orElse(null);
        multiTenant = parametersConfig.tenant().enabled();
        history = new ParametersHistory(instanceId);

        // update
        if (parametersConfig.history().enabled()) {
            scheduler.newJob("parameters-history")
                    .setCron(parametersConfig.history().updateSchedule())
                    .setConcurrentExecution(Scheduled.ConcurrentExecution.SKIP)
                    .setTask(this::sendHistory)
                    .schedule();
        }
    }

    private void sendHistory(ScheduledExecution scheduledExecution) {
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

        ParametersBucket pb = new ParametersBucket()
                .start(history.getStart())
                .end(history.getEnd())
                .instanceId(history.getInstanceId());

        parameters.getParameters().forEach((k, v) -> {
            pb.putParametersItem(k, new ParameterInfo()
                    .count(v.getCount().get())
                    .currentValue(v.getCurrentValue())
                    .defaultValue(v.getDefaultValue()));
        });

        try (var response = client.bucketRequest(config.productName(), config.applicationId(), pb)) {
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
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
