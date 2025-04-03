package org.tkit.onecx.quarkus.it.parameter;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.HashMap;
import java.util.Map;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;

import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
@QuarkusTestResource(MockServerTestResource.class)
class ParametersCustomTest extends AbstractTest {

    @Test
    void testParamTest() {

        var tenantId = "200";
        var token = createToken(tenantId);

        var data = new HashMap<>(Map.of(
                "PARAM_TEXT_3", Map.of("a", "1"),
                "PARAM_TEXT", "Text Information",
                "PARAM_TEXT_2", "4321",
                "PARAM_NUMBER", 123,
                "PARAM_BOOL", true));

        addExpectation(
                mockServerClient
                        .when(request()
                                .withPath("/v1/test1/app1/parameters")
                                .withMethod(HttpMethod.GET)
                                .withHeader(APM_HEADER_PARAM, token))
                        .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data))));

        var result = getTestParam(token);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("text", result.getA());
        Assertions.assertEquals(100, result.getB());
        Assertions.assertEquals(true, result.isC());

        await().atMost(10, SECONDS)
                .until(() -> getHistory(tenantId).stream().map(x -> x.getParameters().size()).reduce(0, Integer::sum) == 3);

        var t = new TestParam();
        t.setA("A100");
        t.setB(-1);
        t.setC(false);
        data.put("PARAM_TEXT_4", t);

        addExpectation(
                mockServerClient
                        .when(request()
                                .withPath("/v1/test1/app1/parameters")
                                .withMethod(HttpMethod.GET)
                                .withHeader(APM_HEADER_PARAM, token))
                        .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data))));

        await().atMost(10, SECONDS)
                .until(() -> {
                    var r = getTestParam(token);
                    return t.getA().equals(r.getA());
                });

        result = getTestParam(token);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(t.getA(), result.getA());
        Assertions.assertEquals(t.getB(), result.getB());
        Assertions.assertEquals(t.isC(), result.isC());

        // check metrics
        var metrics = given()
                .when()
                .get(RestAssured.baseURI + "/q/metrics")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().response().asString();

        Assertions.assertTrue(metrics.contains("# HELP onecx_parameters Number of times parameter have been used"));
        Assertions.assertTrue(metrics.contains("onecx_parameters_total{name="));
    }

    private TestParam getTestParam(String token) {
        return given()
                .when()
                .header(APM_HEADER_PARAM, token)
                .contentType(APPLICATION_JSON)
                .get("testParam")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(TestParam.class);
    }

}
