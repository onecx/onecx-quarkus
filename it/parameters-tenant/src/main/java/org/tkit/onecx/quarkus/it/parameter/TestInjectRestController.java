package org.tkit.onecx.quarkus.it.parameter;

import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.quarkus.parameter.Parameter;

@Path("inject/parameters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestInjectRestController {

    @Parameter(name = "I_PARAM_TEXT")
    Instance<String> paramString;

    @Parameter(name = "I_PARAM_NUMBER")
    Instance<Integer> paramInteger;

    @Parameter(name = "I_PARAM_BOOL")
    Instance<Boolean> paramBoolean;

    @Parameter(name = "I_PARAM_TEXT_4")
    Instance<TestParam> testParam;

    @Parameter(name = "I_DOES_NOT_EXISTS_1")
    Instance<String> doesNotExists1;

    @Parameter(name = "I_DOES_NOT_EXISTS_2")
    Instance<Boolean> doesNotExists2;

    @GET
    @Path("{name}/{type}")
    public Response parameters(@PathParam("name") String name, @PathParam("type") String type) {

        Object value = switch (type) {
            case "String" -> paramString.get();
            case "Integer" -> paramInteger.get();
            case "Boolean" -> paramBoolean.get();
            default -> "NO_VALUE";
        };

        return Response.ok(value).build();
    }

    @GET
    @Path("testParam")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testParam() {

        var tmp1 = doesNotExists1.get();
        var tmp2 = doesNotExists2.get();
        var tmp3 = testParam.get();
        return Response.ok(tmp3).build();
    }
}
