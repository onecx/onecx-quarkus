package org.tkit.onecx.quarkus.it.parameter;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.mock.Expectation;
import org.mockserver.model.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import gen.org.tkit.onecx.parameters.v1.model.ParametersBucket;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(MockServerTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTest {

    protected static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @InjectMockServerClient
    protected MockServerClient mockServerClient;

    protected Map<String, List<ParametersBucket>> histories = new HashMap<>();

    private final List<String> ids = new ArrayList<>();
    private final List<String> historyIds = new ArrayList<>();

    private static final String NO_TENANT = "none";

    @BeforeAll
    void beforeAll() {
        historyIds.clear();
        var e = mockServerClient
                .when(request().withPath("/v1/test1/app1/history").withMethod(HttpMethod.POST))
                .respond(httpRequest -> {
                    var data = mapper.readValue(httpRequest.getBodyAsJsonOrXmlString(), ParametersBucket.class);
                    histories.computeIfAbsent(NO_TENANT, x -> new ArrayList<>()).add(data);
                    histories.forEach((x, d) -> {
                        d.forEach(b -> {
                            System.out.println("#### " + x + " KEYS: " + b.getParameters().keySet());
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

    protected List<ParametersBucket> getHistory() {
        var tmp = histories.get(NO_TENANT);
        if (tmp != null) {
            return tmp;
        }
        return List.of();
    }
}
