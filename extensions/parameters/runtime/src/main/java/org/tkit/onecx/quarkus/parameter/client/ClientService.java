package org.tkit.onecx.quarkus.parameter.client;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.history.ParametersHistory;

import gen.org.tkit.onecx.parameters.v1.api.ParameterV1Api;
import gen.org.tkit.onecx.parameters.v2.api.ParameterV2Api;
import gen.org.tkit.onecx.parameters.v2.model.ParameterInfo;
import gen.org.tkit.onecx.parameters.v2.model.ParametersBucket;

@Singleton
public class ClientService {

    @Inject
    @RestClient
    ParameterV1Api clientV1;

    @Inject
    @RestClient
    ParameterV2Api client;

    @Inject
    ParametersConfig config;

    public Response getParameters(String productName, String appId) {
        if (config.clientV1()) {
            return clientV1.getParameters(productName, appId);
        }
        return client.getParameters(productName, appId);
    }

    public Response bucketRequest(String productName, String appId, ParametersHistory history,
            ParametersHistory.TenantParameters parameters) {

        if (config.clientV1()) {
            gen.org.tkit.onecx.parameters.v1.model.ParametersBucket pb = new gen.org.tkit.onecx.parameters.v1.model.ParametersBucket()
                    .start(history.getStart())
                    .end(history.getEnd())
                    .instanceId(history.getInstanceId());

            parameters.getParameters().forEach((k, v) -> {
                pb.putParametersItem(k, new gen.org.tkit.onecx.parameters.v1.model.ParameterInfo()
                        .count(v.getCount().get())
                        .currentValue(v.getCurrentValue())
                        .defaultValue(v.getDefaultValue()));
            });

            return clientV1.bucketRequest(productName, appId, pb);
        }

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

        return client.bucketRequest(productName, appId, pb);
    }
}
