package org.tkit.onecx.quarkus.it.parameter;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.quarkus.parameter.ParametersService;

@Path("parameters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestRestController {

    @Inject
    ParametersService parametersService;

    @GET
    @Path("{name}/{type}")
    public Response parameters(@PathParam("name") String name, @PathParam("type") String type) {

        Object value = switch (type) {
            case "String" -> parametersService.getValue(name, String.class, "NO_STRING_VALUE");
            case "Integer" -> parametersService.getValue(name, Integer.class, Integer.MIN_VALUE);
            case "Boolean" -> parametersService.getValue(name, Boolean.class, Boolean.FALSE);
            default -> "NO_VALUE";
        };

        return Response.ok(value).build();
    }

    @GET
    @Path("testParam")
    public Response testParam() {
        parametersService.getValue("DOES_NOT_EXISTS_1", String.class, "{\"x\": true, \"data\": {\"a\": 100 }}");
        parametersService.getValue("DOES_NOT_EXISTS_2", boolean.class, true);
        return Response.ok(parametersService.getValue("PARAM_TEXT_4", TestParam.class)).build();
    }
}
