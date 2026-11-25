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

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logmanager.ExtLogRecord;
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
            var level = logRecord.getLevel().intValue();
            if (level >= Level.WARNING.intValue()) {
                messages.add("WARN " + format(logRecord));
            } else if (level >= Level.FINE.intValue()) {
                messages.add("DEBUG " + format(logRecord));
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

    static String format(LogRecord logRecord) {
        return logRecord instanceof ExtLogRecord ext ? ext.getFormattedMessage() : logRecord.getMessage();
    }

    static String permissionServiceUrl() {
        return ConfigProvider.getConfig().getValue("quarkus.rest-client.onecx_permission.url", String.class)
                + "/v1/permissions/user/test1/app-id";
    }

    @BeforeEach
    void addHandler() {
        Logger.getLogger(LOGGER).addHandler(handler);
    }

    @AfterEach
    void removeHandler() {
        Logger.getLogger(LOGGER).removeHandler(handler);
    }

    private void get(String path, String user, String principalToken, Response.Status status) {
        var request = given().when().contentType(APPLICATION_JSON);
        if (user != null) {
            request.auth().oauth2(keycloakClient.getAccessToken(user));
        }
        if (principalToken != null) {
            request.header(APM_PRINCIPAL_TOKEN_HEADER, principalToken);
        }
        request.get(path).then().statusCode(status.getStatusCode());
    }

    @Test
    void missingPermissionTest() {
        get("write", "bob", "token-data-2", Response.Status.FORBIDDEN);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(messages).containsSubsequence(
                "WARN Permission denied, missing [onecx:resource1#admin-write]",
                "DEBUG Granted permissions [resource2#admin-write, resource2#admin-read]"));
    }

    @Test
    void missingPermissionWithoutGrantedPermissionsTest() {
        get("write", "bob", null, Response.Status.FORBIDDEN);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(messages).containsSubsequence(
                "WARN Permission denied, missing [onecx:resource1#admin-write]",
                "DEBUG Granted permissions []"));
    }

    @Test
    void missingPermissionWithoutActionTest() {
        get("no-action", "bob", "token-data-1", Response.Status.FORBIDDEN);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(messages)
                .contains("WARN Permission denied, missing [other]"));
    }

    @Test
    void allPermissionsMissingTest() {
        get("write-or-read", "bob", "token-data-x-1", Response.Status.FORBIDDEN);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(messages)
                .anySatisfy(m -> assertThat(m)
                        .isIn("WARN Permission denied, missing [onecx:resource1#admin-write,resource2#admin-read]",
                                "WARN Permission denied, missing [onecx:resource2#admin-read,resource1#admin-write]")));
    }

    @Test
    void onePermissionGrantedNoWarningTest() {
        get("write-or-read", "bob", "token-data-2", Response.Status.OK);

        assertThat(messages).noneMatch(m -> m.contains("Permission denied"));
    }

    @Test
    void roleDeniedNoPermissionWarningTest() {
        get("admin", "bob", "token-data-1", Response.Status.FORBIDDEN);

        assertThat(messages).noneMatch(m -> m.contains("Permission denied"));
    }

    @Test
    void unauthorizedNoPermissionWarningTest() {
        get("write", null, "token-data-1", Response.Status.UNAUTHORIZED);

        assertThat(messages).noneMatch(m -> m.contains("Permission denied"));
    }

    @Test
    void permissionServiceConnectionErrorTest() {
        get("write", "bob", "token-drop-connection", Response.Status.FORBIDDEN);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(messages).anySatisfy(m -> assertThat(m)
                .startsWith("WARN Failed to load permissions from permission service, status null, url "
                        + permissionServiceUrl() + ", error ")));
        assertThat(messages).noneMatch(m -> m.contains("Permission denied"));
    }

    @Test
    void permissionServiceErrorTest() {
        get("write", "bob", "token-error", Response.Status.FORBIDDEN);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(messages).anySatisfy(m -> assertThat(m)
                .startsWith("WARN Failed to load permissions from permission service, status 500, url "
                        + permissionServiceUrl() + ", error ")
                .contains("status code 500")));
        assertThat(messages).noneMatch(m -> m.contains("Permission denied"));
    }

    @Test
    void grantedNoWarningTest() {
        get("write", "bob", "token-data-1", Response.Status.OK);

        assertThat(messages).noneMatch(m -> m.contains("Permission denied"));
    }
}
