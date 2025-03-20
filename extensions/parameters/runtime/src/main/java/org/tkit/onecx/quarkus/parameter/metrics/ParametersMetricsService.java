package org.tkit.onecx.quarkus.parameter.metrics;

import java.util.HashMap;
import java.util.Map;

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
public class ParametersMetricsService {

    private static final Logger log = LoggerFactory.getLogger(ParametersMetricsService.class);

    @Inject
    ParameterClientService client;

    @Inject
    Vertx vertx;

    // The bucket contains the current collected requests
    private ParametersBucketHolder bucket;

    // cache description of the parameters
    static Map<String, String> descriptions = new HashMap<>();

    public void init(ParametersConfig parametersConfig) {

        parametersConfig.parameters().forEach((k, v) -> descriptions.put(k, v.description().orElse(null)));

        bucket = new ParametersBucketHolder();

        // init rest client
        String instanceId = parametersConfig.instanceId().orElse(null);

        // update
        vertx.setPeriodic(parametersConfig.metrics().metricsIntervalInMilliseconds(), id -> {
            ParametersBucketHolder tmp = this.bucket;
            this.bucket = new ParametersBucketHolder();
            tmp.setInstanceId(instanceId);
            tmp.end();

            // do not send empty bucket
            if (tmp.getBucket().isEmpty()) {
                return;
            }

            client.sendMetrics(tmp.getBucket())
                    .onItem().transform(resp -> {
                        if (resp.getStatus() != Response.Status.OK.getStatusCode()) {
                            log.error("Error send metrics to the parameters management. Code: {}", resp.getStatus());
                        }
                        return resp.getStatus();
                    })
                    .onFailure().recoverWithItem(ex -> {
                        log.error("Error send metrics to the parameters management. Error: {}", ex.getMessage());
                        return -1;
                    }).subscribe().with(r -> log.debug("Send metrics. Code: {}", r));
        });
    }

    @ConsumeEvent(ParameterEvent.NAME)
    public void consumeEvent(ParameterEvent event) {
        bucket.addParameterRequest(event.name, event.defaultValue, event.value, descriptions.get(event.name));
    }

}
