package org.tkit.onecx.quarkus.it.permision;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.quarkus.it.permission.TestImpl;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TestImpl.class)
class TestImplTest {

    @Test
    void test1() {

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("1")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }
}
