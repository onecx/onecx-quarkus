package org.tkit.onecx.quarkus.it.parameter;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpResponse.response;

import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.*;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
class ParametersTest extends AbstractTest {

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
