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
@TestHTTPEndpoint(RoleRestController.class)
class RoleRestControllerTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final String USER = "bob";

    private static final String APM_PRINCIPAL_TOKEN_HEADER = ConfigProvider.getConfig()
            .getValue("onecx.permissions.token-header-param", String.class);

    @Test
    void errorTest() {
        var token = keycloakClient.getAccessToken(USER);
        given()
                .when()
                .auth().oauth2(token)
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-x-2")
                .contentType(APPLICATION_JSON)
                .get("1")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .when()
                .auth().oauth2(token)
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-x-2")
                .contentType(APPLICATION_JSON)
                .get("1")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void getTest() {
        given()
                .when()
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-1")
                .contentType(APPLICATION_JSON)
                .get("3")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken("alice"))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-1")
                .contentType(APPLICATION_JSON)
                .get("2")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-x-1")
                .contentType(APPLICATION_JSON)
                .get("1")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

}
