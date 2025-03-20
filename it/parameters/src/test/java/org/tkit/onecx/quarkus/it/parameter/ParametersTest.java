package org.tkit.onecx.quarkus.it.parameter;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.mock.Expectation;
import org.mockserver.model.MediaType;

import gen.org.tkit.onecx.parameters.v1.model.ParametersBucket;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParametersTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    List<ParametersBucket> histories = new ArrayList<>();

    private Expectation[] mocks = new Expectation[0];

    @BeforeAll
    void init() {

        mocks = mockServerClient
                .when(request().withPath("/v1/test1/app1/history").withMethod(HttpMethod.POST))
                .respond(httpRequest -> {
                    var data = mapper.readValue(httpRequest.getBodyAsJsonOrXmlString(), ParametersBucket.class);
                    histories.add(data);
                    return response().withStatusCode(Response.Status.OK.getStatusCode())
                            .withContentType(MediaType.APPLICATION_JSON);
                });
    }

    @AfterAll
    void after() {
        for (var m : mocks) {
            mockServerClient.clear(m.getAction().getExpectationId());
        }
    }

    private static final String STRING_TYPE = "String";

    @Test
    void loadParametersTest() {

        call(Map.of("name", "test", "type", STRING_TYPE), "NO_STRING_VALUE");
        call(Map.of("name", "PARAM_TEXT", "type", STRING_TYPE), "Text Information");
        call(Map.of("name", "PARAM_TEXT_2", "type", STRING_TYPE), "4321");
        call(Map.of("name", "PARAM_NUMBER", "type", "Integer"), "123");
        call(Map.of("name", "PARAM_BOOL", "type", "Boolean"), "true");

        await().atMost(15, SECONDS)
                .until(() -> histories.stream().map(x -> x.getParameters().size()).reduce(0, Integer::sum) == 5);
    }

    private void call(Map<String, String> params, String expected) {
        var result = given()
                .when()
                .pathParams(params)
                .contentType(APPLICATION_JSON)
                .get("{name}/{type}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().response().asString();
        Assertions.assertEquals(expected, result);
    }
}
