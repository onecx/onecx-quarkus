package org.tkit.onecx.quarkus.it.tenant;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("model")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ModelRestController {

    @Inject
    ModelDAO dao;

    @GET
    public Response getAll() {
        return Response.ok(ModelList.create(dao.findAll().toList())).build();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") String id) {
        var model = dao.findById(id);
        if (model == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(model).build();
    }

    public static class ModelList {
        public List<Model> models;

        public static ModelList create(List<Model> models) {
            ModelList m = new ModelList();
            m.models = models;
            return m;
        }

        public List<Model> getModels() {
            return models;
        }
    }
}
