package org.tkit.onecx.quarkus.parameter.client;

import java.util.Map;

import gen.org.tkit.onecx.parameters.v1.api.ParameterV1Api;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import gen.org.tkit.onecx.parameters.v1.model.ParameterInfo;
import gen.org.tkit.onecx.parameters.v1.model.ParametersBucket;
import io.smallrye.mutiny.Uni;
import org.tkit.onecx.quarkus.parameter.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.metrics.ParametersBucketItem;

@ApplicationScoped
public class ParameterClient {

    @Inject
    ParametersConfig config;

    @Inject
    @RestClient
    ParameterV1Api client;

    public Uni<Map<String, String>> getApplicationParameters() {
        return client.getParameters(config.productName(), config.applicationId());
    }

    public Uni<Response> sendMetrics(ParametersBucketItem bucket) {

        ParametersBucket pb = new ParametersBucket()
                .start(bucket.getStart())
                .end(bucket.getEnd())
                .instanceId(bucket.getInstanceId());

        for (Map.Entry<String, ParametersBucketItem.ParameterInfoItem> item : bucket.getParameters().entrySet()) {
            var v = item.getValue();
            pb.putParametersItem(item.getKey(), new ParameterInfo()
                    .count(v.getCount().get())
                    .description(v.getDescription())
                    .currentValue(v.getCurrentValue())
                    .defaultValue(v.getDefaultValue()));
        }

        return client.bucketRequest(config.productName(), config.applicationId(), pb);
    }
}
