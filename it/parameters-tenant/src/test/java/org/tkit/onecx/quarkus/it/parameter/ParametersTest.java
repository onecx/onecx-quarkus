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

        var tenantId = "100";
        var token = createToken(tenantId);

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
                                .withMethod(HttpMethod.GET)
                                .withHeader(APM_HEADER_PARAM, token))
                        .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data))));

        call(token, Map.of("name", "test", "type", STRING_TYPE), "NO_STRING_VALUE");
        call(token, Map.of("name", "A_PARAM_TEXT", "type", STRING_TYPE), "Text Information");
        call(token, Map.of("name", "A_PARAM_TEXT_2", "type", STRING_TYPE), "4321");
        call(token, Map.of("name", "A_PARAM_NUMBER", "type", "Integer"), "123");
        call(token, Map.of("name", "A_PARAM_BOOL", "type", "Boolean"), "true");

        await().atMost(15, SECONDS)
                .until(() -> getHistory(tenantId).stream().map(x -> x.getParameters().size()).reduce(0, Integer::sum) == 5);
    }

    private void call(String token, Map<String, String> params, String expected) {
        var result = given()
                .when()
                .header(APM_HEADER_PARAM, token)
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
