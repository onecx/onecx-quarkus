package org.tkit.onecx.quarkus.it.parameter;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.*;

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
