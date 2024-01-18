package io.github.onecx.quarkus.it.permision;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
class TestRestControllerTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final String ADMIN = "alice";

    private static final String USER = "bob";

    private static final String APM_PRINCIPAL_TOKEN_HEADER = ConfigProvider.getConfig()
            .getValue("%test.onecx.permissions.token-header-param", String.class);

    @Test
    void openTest() {

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("open")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    void adminTest() {

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-1")
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
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-1")
                .contentType(APPLICATION_JSON)
                .get("write")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-2")
                .contentType(APPLICATION_JSON)
                .get("write")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());
    }
}
