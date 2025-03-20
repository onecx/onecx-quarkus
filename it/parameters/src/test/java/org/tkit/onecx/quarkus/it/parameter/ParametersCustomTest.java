package org.tkit.onecx.quarkus.it.parameter;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
import org.mockserver.mock.Expectation;
import org.mockserver.model.MediaType;

import gen.org.tkit.onecx.parameters.v1.model.ParametersBucket;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(TestRestController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(MockServerTestResource.class)
class ParametersCustomTest extends AbstractTest {

    @InjectMockServerClient
    MockServerClient mockServerClient;

    List<ParametersBucket> histories = new ArrayList<>();

    private Expectation[] mocks = new Expectation[0];

    @BeforeAll
    void init() {
        mocks = mockServerClient
                .when(request().withPath("/v1/test1/app1/history").withMethod(HttpMethod.POST))
                .respond(httpRequest -> {
                    var data = mapper.readValue(httpRequest.getBodyAsJsonOrXmlString(), ParametersBucket.class);
                    histories.add(data);
                    return response().withStatusCode(Response.Status.OK.getStatusCode())
                            .withContentType(MediaType.APPLICATION_JSON);
                });
    }

    @AfterAll
    void after() {
        for (var m : mocks) {
            mockServerClient.clear(m.getAction().getExpectationId());
        }
    }

    @Test
    void testParamTest() {

        var result = given()
                .when()
                .contentType(APPLICATION_JSON)
                .get("testParam")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .log().all()
                .extract().as(TestParam.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("text", result.getA());
        Assertions.assertEquals(100, result.getB());
        Assertions.assertEquals(true, result.isC());

        await().atMost(15, SECONDS)
                .until(() -> histories.stream().map(x -> x.getParameters().size()).reduce(0, Integer::sum) == 3);
    }
}
