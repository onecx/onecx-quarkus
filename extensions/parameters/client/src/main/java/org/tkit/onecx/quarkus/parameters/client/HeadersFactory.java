package org.tkit.onecx.quarkus.parameters.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;
import org.tkit.quarkus.context.ApplicationContext;

@ApplicationScoped
public class HeadersFactory extends DefaultClientHeadersFactoryImpl {

    @ConfigProperty(name = "onecx.parameters.token-header-param")
    String tokenHeaderParam;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> tmp = super.update(incomingHeaders, clientOutgoingHeaders);

        var ctx = ApplicationContext.get();

        if (ctx != null) {
            var token = ctx.getPrincipalToken();
            if (token != null && !tmp.containsKey(tokenHeaderParam)) {
                tmp.putSingle(tokenHeaderParam, token.getRawToken());
            }
        }
        return tmp;
    }
}
