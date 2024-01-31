package org.tkit.onecx.quarkus.permission.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;

@ApplicationScoped
public class PermissionDefaultClientHeadersFactoryImpl extends DefaultClientHeadersFactoryImpl {

    @Inject
    RequestHeaderContainer headerContainer;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {

        incomingHeaders = headerContainer.getHeaders();
        MultivaluedMap<String, String> propagatedHeaders = super.update(incomingHeaders, clientOutgoingHeaders);

        var tokenHeaderParam = headerContainer.getTokenHeaderParam();
        if (tokenHeaderParam != null && !propagatedHeaders.containsKey(tokenHeaderParam)
                && incomingHeaders.containsKey(tokenHeaderParam)) {
            propagatedHeaders.put(tokenHeaderParam, incomingHeaders.get(tokenHeaderParam));
        }

        return propagatedHeaders;
    }
}
