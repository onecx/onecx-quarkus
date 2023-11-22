package io.github.onecx.quarkus.apm.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;

import io.github.onecx.quarkus.apm.ApmConfig;
import io.github.onecx.quarkus.apm.ApmHeaderContainer;

@ApplicationScoped
public class ApmDefaultClientHeadersFactory extends DefaultClientHeadersFactoryImpl {

    @Inject
    ApmConfig config;

    @Inject
    ApmHeaderContainer headerContainer;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {

        incomingHeaders = headerContainer.getHeaders();
        MultivaluedMap<String, String> propagatedHeaders = super.update(incomingHeaders, clientOutgoingHeaders);

        var tokenHeaderParam = config.tokenHeaderParam();
        if (!propagatedHeaders.containsKey(tokenHeaderParam) && incomingHeaders.containsKey(tokenHeaderParam)) {
            propagatedHeaders.put(tokenHeaderParam, incomingHeaders.get(tokenHeaderParam));
        }

        return propagatedHeaders;
    }

}
