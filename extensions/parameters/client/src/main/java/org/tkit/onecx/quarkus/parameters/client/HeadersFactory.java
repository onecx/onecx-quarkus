package org.tkit.onecx.quarkus.parameters.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;
import org.tkit.quarkus.context.ApplicationContext;

@ApplicationScoped
public class HeadersFactory extends DefaultClientHeadersFactoryImpl {

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> tmp = super.update(incomingHeaders, clientOutgoingHeaders);

        var ctx = ApplicationContext.get();
        if (ctx != null) {
            var token = ctx.getPrincipalToken();
            if (token != null) {
                tmp.putSingle("apm-principal-token", token.getRawToken());
            }
        }
        return tmp;
    }
}
