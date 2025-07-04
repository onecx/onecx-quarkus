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

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
@QuarkusTestResource(MockServerTestResource.class)
class ParametersCustomTest extends AbstractTest {

    @Test
    void testParamTest() {

        var data = new HashMap<>(Map.of(
                "C_PARAM_TEXT_3", Map.of("a", "1"),
                "C_PARAM_TEXT", "Text Information",
                "C_PARAM_TEXT_2", "4321",
                "C_PARAM_NUMBER", 123,
                "C_PARAM_BOOL", true));

        addExpectation(
                mockServerClient
                        .when(request()
                                .withPath("/v2/parameters/test1/app1")
                                .withMethod(HttpMethod.GET))
                        .withPriority(100)
                        .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data))));

        var result = getTestParam();

        Assertions.assertNotNull(result);
        Assertions.assertEquals("text", result.getA());
        Assertions.assertEquals(100, result.getB());
        Assertions.assertTrue(result.isC());

        await().atMost(10, SECONDS)
                .until(() -> getHistory().stream().map(x -> x.getParameters().size()).reduce(0, Integer::sum) == 3);

        var t = new TestParam();
        t.setA("A100");
        t.setB(-1);
        t.setC(false);
        data.put("C_PARAM_TEXT_4", t);

        addExpectation(
                mockServerClient
                        .when(request()
                                .withPath("/v2/parameters/test1/app1")
                                .withMethod(HttpMethod.GET))
                        .withPriority(102)
                        .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                                .withContentType(MediaType.APPLICATION_JSON)
                                .withBody(JsonBody.json(data))));

        await().atMost(10, SECONDS)
                .until(() -> {
                    var r = getTestParam();
                    return t.getA().equals(r.getA());
                });

        result = getTestParam();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(t.getA(), result.getA());
        Assertions.assertEquals(t.getB(), result.getB());
        Assertions.assertEquals(t.isC(), result.isC());
    }

    private TestParam getTestParam() {
        return given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("testParam")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(TestParam.class);
    }

}
