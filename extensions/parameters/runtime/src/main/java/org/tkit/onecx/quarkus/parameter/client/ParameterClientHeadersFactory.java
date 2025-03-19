package org.tkit.onecx.quarkus.parameter.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;
import org.tkit.onecx.quarkus.parameter.ParametersConfig;
import org.tkit.quarkus.rs.context.RestContextHeaderContainer;
import org.tkit.quarkus.rs.context.token.TokenContextConfig;

@ApplicationScoped
public class ParameterDefaultClientHeadersFactoryImpl extends DefaultClientHeadersFactoryImpl {

    @Inject
    ParametersConfig config;

    @Inject
    RestContextHeaderContainer headerContainer;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {

        return super.update(incomingHeaders, clientOutgoingHeaders);
//        var tmp = headerContainer.getHeaders();
//        MultivaluedMap<String, String> propagatedHeaders = super.update(tmp, clientOutgoingHeaders);

//        var tokenHeaderParam = config.token().tokenHeaderParam();
//        if (!propagatedHeaders.containsKey(tokenHeaderParam) && tmp.containsKey(tokenHeaderParam)) {
//            propagatedHeaders.put(tokenHeaderParam, tmp.get(tokenHeaderParam));
//        }

//        return propagatedHeaders;
    }
}
