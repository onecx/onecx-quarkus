package io.github.onecx.quarkus.apm;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ApmDefaultClientHeadersFactory extends DefaultClientHeadersFactoryImpl {

    public static final String APM_HEADER_PRINCIPAL_TOKEN_NAME = "apm-principal-token";
    private static final Logger log = LoggerFactory.getLogger(ApmDefaultClientHeadersFactory.class);

    @ConfigProperty(name = "onecx.apm.header-apm-principal-token-name")
    Optional<String> tokenName;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {

        var headers = RequestContextHolder.close().getHeaders();
        var propagatedHeaders = super.update(headers, clientOutgoingHeaders);

        log.debug("APM client update header in: {} client: {}", headers, clientOutgoingHeaders);

        if (tokenName.isPresent()) {
            String header = tokenName.get();
            if (headers.containsKey(header)) {
                propagatedHeaders.put(APM_HEADER_PRINCIPAL_TOKEN_NAME, headers.get(header));
            }
        } else {
            // propagate the apm-principal-token
            if (!propagatedHeaders.containsKey(APM_HEADER_PRINCIPAL_TOKEN_NAME)
                    && (headers.containsKey(APM_HEADER_PRINCIPAL_TOKEN_NAME))) {
                propagatedHeaders.put(APM_HEADER_PRINCIPAL_TOKEN_NAME, headers.get(APM_HEADER_PRINCIPAL_TOKEN_NAME));
            }
        }

        log.debug("APM client update header out: {}", propagatedHeaders);

        return propagatedHeaders;
    }

}
