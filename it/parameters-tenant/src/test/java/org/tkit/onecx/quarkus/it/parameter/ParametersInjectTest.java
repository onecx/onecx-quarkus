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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TestInjectRestController.class)
class ParametersInjectTest extends AbstractTest {

    private static final String STRING_TYPE = "String";

    @Test
    void loadParametersTest() {

        var tenantId = "500";
        var token = createToken(tenantId);

        var data = Map.of(
                "I_PARAM_TEXT_3", Map.of("a", "1"),
                "I_PARAM_TEXT", "Inject Text Information",
                "I_PARAM_TEXT_2", "43219",
                "I_PARAM_NUMBER", 1239,
                "I_PARAM_BOOL", true);

        addExpectation(
                mockServerClient
                        .when(request()
                                .withPath("/v1/test1/app1/parameters")
                                .withMethod(HttpMethod.GET)
                                .withHeader(APM_HEADER_PARAM, token))
                        .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data))));

        await().atMost(10, SECONDS)
                .until(() -> {
                    var r = callTest(token, Map.of("name", "I_PARAM_TEXT", "type", STRING_TYPE));
                    return "Inject Text Information".equals(r);
                });

        call(token, Map.of("name", "I_PARAM_TEXT", "type", STRING_TYPE), "Inject Text Information");
        call(token, Map.of("name", "I_PARAM_NUMBER", "type", "Integer"), "1239");
        call(token, Map.of("name", "I_PARAM_BOOL", "type", "Boolean"), "true");

        await().atMost(15, SECONDS)
                .until(() -> getHistory(tenantId).stream().map(x -> x.getParameters().entrySet()
                        .stream().filter(a -> a.getKey().startsWith("I_")).count()).reduce(0L, Long::sum) == 3);
    }

    private void call(String token, Map<String, String> params, String expected) {
        var result = callTest(token, params);
        Assertions.assertEquals(expected, result);
    }

    private String callTest(String token, Map<String, String> params) {
        return given()
                .when()
                .header(APM_HEADER_PARAM, token)
                .pathParams(params)
                .contentType(APPLICATION_JSON)
                .get("{name}/{type}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().response().asString();
    }
}
