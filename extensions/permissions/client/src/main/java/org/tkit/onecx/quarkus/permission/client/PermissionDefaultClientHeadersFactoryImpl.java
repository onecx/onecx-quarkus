package org.tkit.onecx.quarkus.permission.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;

import io.vertx.core.Vertx;

@ApplicationScoped
public class PermissionDefaultClientHeadersFactoryImpl extends DefaultClientHeadersFactoryImpl {

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {

        var ctx = Vertx.currentContext();

        RequestHeaderContainer headerContainer = ctx.getLocal(RequestHeaderContainer.class.getName());
        ctx.removeLocal(RequestHeaderContainer.class.getName());

        incomingHeaders = headerContainer.getIncomingHeaders();
        MultivaluedMap<String, String> propagatedHeaders = super.update(incomingHeaders, clientOutgoingHeaders);

        var tokenHeaderParam = headerContainer.getTokenHeaderParam();
        if (tokenHeaderParam != null && !propagatedHeaders.containsKey(tokenHeaderParam)
                && incomingHeaders.containsKey(tokenHeaderParam)) {
            propagatedHeaders.put(tokenHeaderParam, incomingHeaders.get(tokenHeaderParam));
        }

        return propagatedHeaders;
    }
}
