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

import org.jboss.logmanager.ExtLogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
class PermissionDeniedLogTest {

    private static final String LOGGER = "org.tkit.onecx.quarkus.permission";

    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    final List<String> messages = new CopyOnWriteArrayList<>();

    final Handler handler = new Handler() {
        @Override
        public void publish(LogRecord logRecord) {
            var level = logRecord.getLevel().intValue();
            var message = logRecord instanceof ExtLogRecord ext ? ext.getFormattedMessage() : logRecord.getMessage();
            if (level >= Level.WARNING.intValue()) {
                messages.add("WARN " + message);
            } else if (level >= Level.FINE.intValue()) {
                messages.add("DEBUG " + message);
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
    void missingMockPermissionTest() {
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken("alice"))
                .contentType(APPLICATION_JSON)
                .get("write")
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode());

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(messages).containsSubsequence(
                "WARN Permission denied, missing [onecx:resource1#admin-write]",
                "DEBUG Granted permissions []"));
    }

    @Test
    void grantedMockPermissionNoWarningTest() {
        given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken("bob"))
                .contentType(APPLICATION_JSON)
                .get("write")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        assertThat(messages).noneMatch(m -> m.contains("Permission denied"));
    }
}
