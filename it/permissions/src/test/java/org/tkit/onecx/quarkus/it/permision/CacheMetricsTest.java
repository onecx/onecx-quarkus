package org.tkit.onecx.quarkus.it.permision;

import static io.restassured.RestAssured.given;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CacheMetricsTest {
    @Test
    void pingMetrics() {
        var response = given()
                .when()
                .get("/q/metrics")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().asString();

        // check extended agroal metrics
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("cache_size{cache=\"onecx-permissions\"}"));
    }
}
