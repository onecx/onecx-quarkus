package org.tkit.onecx.quarkus.it.parameter;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
class ParametersTest {

    private static final String STRING_TYPE = "String";

    static Stream<Arguments> loadParameters() {
        return Stream.of(
                Arguments.of(Map.of("name", "test", "type", STRING_TYPE), "NO_STRING_VALUE"),
                Arguments.of(Map.of("name", "PARAM_TEXT", "type", STRING_TYPE), "Text Information"),
                Arguments.of(Map.of("name", "PARAM_TEXT_2", "type", STRING_TYPE), "4321"),
                Arguments.of(Map.of("name", "PARAM_NUMBER", "type", "Integer"), "123"),
                Arguments.of(Map.of("name", "PARAM_BOOL", "type", "Boolean"), "true"));
    }

    @ParameterizedTest
    @MethodSource("loadParameters")
    void loadParametersTest(Map<String, String> params, String expected) {

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

    @Test
    void testParamTest() {

        var result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("testParam")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .log().all()
                .extract().as(TestParam.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("text", result.getA());
        Assertions.assertEquals(100, result.getB());
        Assertions.assertEquals(true, result.isC());
    }
}
