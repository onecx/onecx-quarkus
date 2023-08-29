package io.github.onecx.quarkus.it.parameter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.Vertx;

@Singleton
public class ParametersMetricsService {

    private static final Logger log = LoggerFactory.getLogger(ParametersMetricsService.class);

    @Inject
    @RestClient
    ParameterRestClient client;

    @Inject
    Vertx vertx;

    // The bucket contains the current collected requests
    volatile ParametersBucket bucket;

    public void init(ParametersConfig parametersConfig, String applicationId) {

        bucket = new ParametersBucket();

        // init rest client
        String instanceId = parametersConfig.instanceId.orElse(null);

        // update
        vertx.setPeriodic(parametersConfig.metrics.metricsIntervalInMilliseconds, id -> {
            ParametersBucket tmp = this.bucket;
            this.bucket = new ParametersBucket();
            tmp.instanceId = instanceId;
            tmp.end();

            client.sendMetrics(applicationId, tmp)
                    .onItem().transform(resp -> {
                        if (resp.getStatus() != 200) {
                            log.error("Error send metrics to the parameters management. Code: " + resp.getStatus());
                        }
                        return resp.getStatus();
                    })
                    .onFailure().recoverWithItem(ex -> {
                        log.error("Error send metrics to the parameters management. Error: " + ex.getMessage());
                        return -1;
                    }).subscribe().with(r -> log.debug("Send metrics. Code: " + r));
        });
    }

    @ConsumeEvent(ParameterEvent.NAME)
    public void consumeEvent(ParameterEvent event) {
        bucket.addParameterRequest(event.propertyName, event.propertyType, event.defaultValue, event.currentValue);
    }

}
