package io.github.onecx.quarkus.it.security;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import gen.io.github.onecx.quarkus.security.example.model.RoleSearchCriteriaDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(RoleRestController.class)
class RoleRestControllerTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    private static final String USER = "bob";

    private static final String APM_PRINCIPAL_TOKEN_HEADER = "apm-principal-token";

    @Test
    void errorTest() {
        var bob = keycloakClient.getAccessToken(USER);
        var token = keycloakClient.getClientAccessToken();

        given()
                .when()
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-x-2")
                .contentType(APPLICATION_JSON)
                .get("0")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());

        given()
                .when()
                .auth().oauth2(bob)
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
                .delete("2")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        given()
                .when()
                .auth().oauth2(token)
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-x-2")
                .contentType(APPLICATION_JSON)
                .get("2")
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
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-1")
                .contentType(APPLICATION_JSON)
                .get("2")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(USER))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-x-1")
                .contentType(APPLICATION_JSON)
                .get("1")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void getSearchTest() {
        var dto = new RoleSearchCriteriaDTO();

        given()
                .when()
                .contentType(APPLICATION_JSON)
                .body(dto)
                .post("search")
                .then()
                .statusCode(Response.Status.UNAUTHORIZED.getStatusCode());
    }

}
