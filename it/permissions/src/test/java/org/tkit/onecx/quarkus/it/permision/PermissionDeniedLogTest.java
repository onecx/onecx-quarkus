package org.tkit.onecx.quarkus.it.permision;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.quarkus.it.permission.TestRestController;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
class PermissionDeniedLogTest {

    private static final String LOGGER = "org.tkit.onecx.quarkus.permission";

    private static final String APM_PRINCIPAL_TOKEN_HEADER = "apm-principal-token";

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    final List<String> messages = new CopyOnWriteArrayList<>();

    final Handler handler = new Handler() {
        @Override
        public void publish(LogRecord logRecord) {
            if (logRecord.getLevel().intValue() >= Level.WARNING.intValue()) {
                messages.add(logRecord.getMessage());
            }
        }

        @Override
        public void flush() {
            // nothing to flush
        }

        @Override
        public void close() {
            // nothing to close
        }
    };

    @BeforeEach
    void addHandler() {
        Logger.getLogger(LOGGER).addHandler(handler);
    }

    @AfterEach
    void removeHandler() {
        Logger.getLogger(LOGGER).removeHandler(handler);
    }

    @Test
    void missingPermissionTest() {
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken("bob"))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-2")
                .contentType(APPLICATION_JSON)
                .get("write")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(messages)
                .anySatisfy(m -> assertThat(m)
                        .isEqualTo("OneCX permission denied, request GET /test/write, required [onecx:resource1#admin-write], "
                                + "granted 2 permission(s)")));
    }

    @Test
    void permissionServiceErrorTest() {
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken("bob"))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-error")
                .contentType(APPLICATION_JSON)
                .get("write")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(messages).anySatisfy(m -> assertThat(m)
                    .startsWith("Failed to load permissions from onecx-permission-svc"));
            assertThat(messages).anySatisfy(m -> assertThat(m)
                    .isEqualTo("OneCX permission denied, request GET /test/write, required [onecx:resource1#admin-write], "
                            + "permissions could not be loaded from onecx-permission-svc"));
        });
    }

    @Test
    void grantedNoWarningTest() {
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken("bob"))
                .header(APM_PRINCIPAL_TOKEN_HEADER, "token-data-1")
                .contentType(APPLICATION_JSON)
                .get("write")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        assertThat(messages).noneMatch(m -> m.startsWith("OneCX permission denied"));
    }
}
