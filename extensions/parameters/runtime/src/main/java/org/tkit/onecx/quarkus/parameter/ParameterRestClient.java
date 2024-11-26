package org.tkit.onecx.quarkus.parameter;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import gen.org.tkit.onecx.parameters.api.ParameterApi;
import gen.org.tkit.onecx.parameters.model.ParameterInfo;
import gen.org.tkit.onecx.parameters.model.ParametersBucket;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class ParameterRestClient {

    @Inject
    ParametersConfig config;

    @Inject
    @RestClient
    ParameterApi client;

    public Uni<Map<String, String>> getApplicationParameters() {
        return client.getApplicationParameters(config.productName(), config.applicationId());
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
                    .type(v.getType())
                    .currentValue(v.getCurrentValue())
                    .defaultValue(v.getDefaultValue()));
        }

        return client.bucketRequest(config.productName(), config.applicationId(), pb);
    }
}
