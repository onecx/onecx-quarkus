package io.github.onecx.quarkus.tenant.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;
import org.tkit.quarkus.rs.context.RestContextHeaderContainer;
import org.tkit.quarkus.rs.context.token.TokenContextConfig;

@ApplicationScoped
public class TenantDefaultClientHeadersFactoryImpl extends DefaultClientHeadersFactoryImpl {

    @Inject
    TokenContextConfig config;

    @Inject
    RestContextHeaderContainer headerContainer;

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
