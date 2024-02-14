package org.tkit.onecx.quarkus.it.permision;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.quarkus.it.permission.TestEndpointRestController;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(TestEndpointRestController.class)
class TestEndpointRestControllerTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    @Test
    void testCall() {
        var r = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken("bob"))
                .header("apm-principal-token", "token-data-1")
                .contentType(APPLICATION_JSON)
                .get("apm-principal-token")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().asString();

        Assertions.assertEquals("token-data-1", r);

        r = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken("bob"))
                .header("Apm-Principal-Token", "token-data-1")
                .contentType(APPLICATION_JSON)
                .get("Apm-Principal-Token")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().asString();

        Assertions.assertEquals("token-data-1", r);
    }
}
