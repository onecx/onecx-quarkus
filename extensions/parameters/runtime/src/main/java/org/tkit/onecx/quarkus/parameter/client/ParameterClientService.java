package org.tkit.onecx.quarkus.parameter.client;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.history.ParametersHistory;

import gen.org.tkit.onecx.parameters.v1.api.ParameterV1Api;
import gen.org.tkit.onecx.parameters.v1.model.ParameterInfo;
import gen.org.tkit.onecx.parameters.v1.model.ParametersBucket;

@ApplicationScoped
public class ParameterClientService {

    @Inject
    ParametersConfig config;

    @Inject
    @RestClient
    ParameterV1Api client;

    public Map<String, Object> getParameters() {
        return client.getParameters(config.productName(), config.applicationId());
    }

    public Response sendMetrics(ParametersHistory history) {

        ParametersBucket pb = new ParametersBucket()
                .start(history.getStart())
                .end(history.getEnd())
                .instanceId(history.getInstanceId());

        for (Map.Entry<String, ParametersHistory.ParameterInfoItem> item : history.getParameters().entrySet()) {
            var v = item.getValue();
            pb.putParametersItem(item.getKey(), new ParameterInfo()
                    .count(v.getCount().get())
                    .currentValue(v.getCurrentValue())
                    .defaultValue(v.getDefaultValue()));
        }

        return client.bucketRequest(config.productName(), config.applicationId(), pb);
    }
}
