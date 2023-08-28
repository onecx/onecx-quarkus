package io.github.onecx.quarkus.it.apm;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
class ApmTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final String ADMIN = "alice";

    private static final String USER = "bob";

    @Test
    void openTest() {

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("open")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON);
    }

    @Test
    void adminTest() {

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .contentType(APPLICATION_JSON)
                .get("admin")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON);

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .contentType(APPLICATION_JSON)
                .get("admin")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void writeTest() {

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .contentType(APPLICATION_JSON)
                .get("write")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON);
    }
}
