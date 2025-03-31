package org.tkit.onecx.quarkus.parameter.history;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.parameter.client.ParameterClientService;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.Vertx;

@Singleton
public class ParametersHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ParametersHistoryService.class);

    @Inject
    ParameterClientService client;

    @Inject
    Vertx vertx;

    // The history contains the current collected requests
    private ParametersHistory history;

    public void init(ParametersConfig parametersConfig) {

        // init rest client
        String instanceId = parametersConfig.instanceId().orElse(null);

        history = new ParametersHistory(instanceId);

        // update
        if (parametersConfig.history().enabled()) {
            vertx.setPeriodic(parametersConfig.history().updateIntervalInMilliseconds(), id -> {
                ParametersHistory tmp = this.history;
                this.history = new ParametersHistory(instanceId);
                tmp.end();

                // do not send empty bucket
                if (tmp.isEmpty()) {
                    return;
                }

                // send bucket to backend
                try (var response = client.sendMetrics(tmp)) {
                    if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                        log.error("Error send metrics to the parameters management. Code: {}", response.getStatus());
                    }
                    log.debug("Send metrics. Code: {}", response.getStatus());
                } catch (Exception ex) {
                    log.error("Error send metrics to the parameters management. Error: {}", ex.getMessage());
                }
            });
        }
    }

    @ConsumeEvent(ParametersHistoryEvent.NAME)
    public void consumeEvent(ParametersHistoryEvent event) {
        history.addParameterRequest(event.name, event.defaultValue, event.value);
    }

}
