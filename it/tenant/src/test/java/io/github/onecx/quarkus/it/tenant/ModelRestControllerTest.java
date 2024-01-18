package io.github.onecx.quarkus.it.tenant;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ModelRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ModelRestControllerTest extends AbstractTest {

    private static final String APM_HEADER_PARAM = "apm-principal-token";

    private static final String APM_EMPTY = "token-empty";
    private static final String APM_100 = "token-100";
    private static final String APM_200 = "token-200";

    @Test
    void getCacheTest() {

        // empty
        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, "token-response-count")
                .get()
                .then()
                .statusCode(INTERNAL_SERVER_ERROR.getStatusCode());

        // 100
        var dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, "token-response-count")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ModelRestController.ModelList.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getModels()).isNotNull().hasSize(4);

        dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, "token-response-count")
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ModelRestController.ModelList.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getModels()).isNotNull().hasSize(4);

    }

    @Test
    void getAllNoApmTest() {

        // null
        given()
                .contentType(APPLICATION_JSON)
                .get()
                .then().statusCode(INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    void getAllTest() {

        // empty
        var dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, APM_EMPTY)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ModelRestController.ModelList.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getModels()).isNotNull().hasSize(3);

        // 100
        dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, APM_100)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ModelRestController.ModelList.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getModels()).isNotNull().hasSize(2);

        // 200
        dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, APM_200)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ModelRestController.ModelList.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getModels()).isNotNull().hasSize(4);
    }

    @Test
    void findByIdTest() {

        // empty
        var dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, APM_EMPTY)
                .get("default-1")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(Model.class);

        assertThat(dto).isNotNull();

        given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, APM_EMPTY)
                .get("100-1")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, APM_100)
                .get("100-2")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(Model.class);

        assertThat(dto).isNotNull();

        dto = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, APM_200)
                .get("200-3")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(Model.class);

        assertThat(dto).isNotNull();
    }
}
