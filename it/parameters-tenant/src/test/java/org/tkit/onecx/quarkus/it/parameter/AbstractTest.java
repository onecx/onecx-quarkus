package org.tkit.onecx.quarkus.it.parameter;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.mock.Expectation;
import org.mockserver.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import gen.org.tkit.onecx.parameters.v1.model.ParametersBucket;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.util.KeyUtils;

@QuarkusTestResource(MockServerTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    protected static final String APM_HEADER_PARAM = ConfigProvider.getConfig()
            .getValue("%test.tkit.rs.context.token.header-param", String.class);
    protected static final String CLAIMS_ORG_ID = ConfigProvider.getConfig()
            .getValue("%test.tkit.rs.context.tenant-id.mock.claim-org-id", String.class);

    private static final Logger log = LoggerFactory.getLogger(AbstractTest.class);

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @InjectMockServerClient
    protected MockServerClient mockServerClient;

    protected Map<String, List<ParametersBucket>> histories = new HashMap<>();

    private final List<String> ids = new ArrayList<>();
    private final List<String> historyIds = new ArrayList<>();

    @BeforeAll
    void beforeAll() {
        historyIds.clear();
        var e = mockServerClient
                .when(request().withPath("/v1/test1/app1/history").withMethod(HttpMethod.POST))
                .respond(httpRequest -> {
                    var org = "default";
                    var token = httpRequest.getFirstHeader(APM_HEADER_PARAM);
                    if (token != null && !token.isBlank()) {
                        String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
                        var claims = JwtClaims.parse(json);
                        org = claims.getStringClaimValue(CLAIMS_ORG_ID);
                    }
                    var data = mapper.readValue(httpRequest.getBodyAsJsonOrXmlString(), ParametersBucket.class);
                    histories.computeIfAbsent(org, x -> new ArrayList<>()).add(data);
                    histories.forEach((x, d) -> {
                        d.forEach(b -> {
                            log.info("#TEST ### {} KEYS: {}", x, b.getParameters().keySet());
                        });
                    });

                    return response().withStatusCode(Response.Status.OK.getStatusCode())
                            .withContentType(MediaType.APPLICATION_JSON);
                });
        for (var i : e) {
            historyIds.add(i.getId());
        }
    }

    @AfterAll
    void afterAll() {
        for (var m : historyIds) {
            mockServerClient.clear(m);
        }
        historyIds.clear();
    }

    @BeforeEach
    void beforeEach() {
        ids.clear();
    }

    @AfterEach
    void afterEach() {
        for (var m : ids) {
            mockServerClient.clear(m);
        }
        ids.clear();
    }

    protected void addExpectation(Expectation[] e) {
        for (var i : e) {
            ids.add(i.getId());
        }
    }

    protected static String createToken(String organizationId) {
        try {

            String userName = "test-user";
            JsonObjectBuilder claims = Json.createObjectBuilder();
            claims.add(Claims.preferred_username.name(), userName);
            claims.add(Claims.sub.name(), userName);
            if (organizationId != null) {
                claims.add(CLAIMS_ORG_ID, organizationId);
            }
            PrivateKey privateKey = KeyUtils.generateKeyPair(2048).getPrivate();
            return Jwt.claims(claims.build()).sign(privateKey);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected List<ParametersBucket> getHistory(String tenantId) {
        var tmp = histories.get(tenantId);
        if (tmp != null) {
            return tmp;
        }
        return List.of();
    }
}
