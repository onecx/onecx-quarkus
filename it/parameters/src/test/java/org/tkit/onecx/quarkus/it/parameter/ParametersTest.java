package org.tkit.onecx.quarkus.it.parameter;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.Map;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
class ParametersTest extends AbstractTest {

    private static final String STRING_TYPE = "String";

    @Test
    void loadParametersTest() {
        var data = Map.of(
                "A_PARAM_TEXT_3", Map.of("a", "1"),
                "A_PARAM_TEXT", "Text Information",
                "A_PARAM_TEXT_2", "4321",
                "A_PARAM_NUMBER", 123,
                "A_PARAM_BOOL", true);

        addExpectation(
                mockServerClient
                        .when(request()
                                .withPath("/v2/parameters/test1/app1")
                                .withMethod(HttpMethod.GET))
                        .withPriority(100)
                        .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data))));

        await().atMost(10, SECONDS)
                .until(() -> {
                    var r = callTest(Map.of("name", "A_PARAM_TEXT", "type", STRING_TYPE));
                    return "Text Information".equals(r);
                });

        call(Map.of("name", "test", "type", STRING_TYPE), "NO_STRING_VALUE");
        call(Map.of("name", "A_PARAM_TEXT", "type", STRING_TYPE), "Text Information");
        call(Map.of("name", "A_PARAM_TEXT_2", "type", STRING_TYPE), "4321");
        call(Map.of("name", "A_PARAM_NUMBER", "type", "Integer"), "123");
        call(Map.of("name", "A_PARAM_BOOL", "type", "Boolean"), "true");

        await().atMost(15, SECONDS)
                .until(() -> getHistory().stream().map(x -> x.getParameters().entrySet()
                        .stream().filter(a -> a.getKey().startsWith("A_")).count()).reduce(0L, Long::sum) >= 4);
    }

    private void call(Map<String, String> params, String expected) {
        var result = callTest(params);
        Assertions.assertEquals(expected, result);
    }

    private String callTest(Map<String, String> params) {
        return given()
                .when()
                .pathParams(params)
                .contentType(APPLICATION_JSON)
                .get("{name}/{type}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().response().asString();
    }
}
