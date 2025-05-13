package org.tkit.onecx.quarkus.it.validator;

import java.io.File;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import gen.org.tkit.onecx.quarkus.validator.example.*;
import gen.org.tkit.onecx.quarkus.validator.example.model.ProblemDetailResponseDTO;

public class ImageRestController implements ImagesInternalApi {

    @Inject
    ExceptionMapper exceptionMapper;

    @Override
    public Response uploadImageCustomValidation(Integer contentLength, File body) {
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public Response uploadImageDefaultValidation(Integer contentLength, File body) {
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public Response uploadImageParameterValidation(Integer contentLength, File body) {
        return Response.status(Response.Status.CREATED).build();
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

}
