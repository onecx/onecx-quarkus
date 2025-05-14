package org.tkit.onecx.quarkus.it.validator;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.junit.jupiter.api.Test;

import gen.org.tkit.onecx.quarkus.validator.example.model.ProblemDetailResponseDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ImageRestController.class)
class ImageRestControllerTest {

    @Test
    void maxSizeConstraintCustomExceptionTest() {
        byte[] body = new byte[1000000];

        given()
                .when()
                .body(body)
                .contentType("image/png")
                .post("/custom")
                .then()
                .log().all()
                .statusCode(CREATED.getStatusCode());

        body = new byte[1100001];
        new Random().nextBytes(body);

        var exception = given()
                .when()
                .body(body)
                .contentType("image/png")
                .post("/custom")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);
        assertThat(exception.getDetail()).isEqualTo(
                "uploadImageCustomValidation.contentLength: OneCX Size constraint. Provider: openapi Key: notDefined Parameter: notDefined Message: error-message from openapi definition (100 Bytes - 1100000 Bytes)");
    }

    @Test
    void minSizeConstraintCustomExceptionTest() {
        byte[] body = new byte[101];

        given()
                .when()
                .body(body)
                .contentType("image/png")
                .post("/custom")
                .then()
                .statusCode(CREATED.getStatusCode());

        body = new byte[30];
        new Random().nextBytes(body);

        var exception = given()
                .when()
                .body(body)
                .contentType("image/png")
                .post("/custom")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);
        assertThat(exception.getDetail()).isEqualTo(
                "uploadImageCustomValidation.contentLength: OneCX Size constraint. Provider: openapi Key: notDefined Parameter: notDefined Message: error-message from openapi definition (100 Bytes - 1100000 Bytes)");
    }

    @Test
    void maxSizeConstraintDefaultExceptionTest() {
        byte[] body = new byte[100000];
        new Random().nextBytes(body);

        given()
                .when()
                .body(body)
                .contentType("image/png")
                .post("/default")
                .then()
                .statusCode(CREATED.getStatusCode());

        body = new byte[1000001];
        new Random().nextBytes(body);

        var exception = given()
                .when()
                .body(body)
                .contentType("image/png")
                .post("/default")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);
        assertThat(exception.getDetail()).isEqualTo(
                "uploadImageDefaultValidation.contentLength: OneCX Size constraint. Provider: openapi Key: paramNull Parameter: paramNull Message: Size Constraint Violation (1 Bytes - 1000000 Bytes)");
    }

    @Test
    void minSizeConstraintDefaultExceptionTest() {
        byte[] body = new byte[1];
        new Random().nextBytes(body);
        given()
                .when()
                .body(body)
                .contentType("image/png")
                .post("/default")
                .then()
                .statusCode(CREATED.getStatusCode());

        body = new byte[0];
        new Random().nextBytes(body);

        var exception = given()
                .when()
                .body(body)
                .contentType("image/png")
                .post("/default")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);
        assertThat(exception.getDetail()).isEqualTo(
                "uploadImageDefaultValidation.contentLength: OneCX Size constraint. Provider: openapi Key: paramNull Parameter: paramNull Message: Size Constraint Violation (1 Bytes - 1000000 Bytes)");
    }

}
