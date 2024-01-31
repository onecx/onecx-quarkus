package org.tkit.onecx.quarkus.it.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import gen.org.tkit.onecx.quarkus.security.example.RoleInternal2Api;
import gen.org.tkit.onecx.quarkus.security.example.model.RoleSearchCriteriaDTO;

@ApplicationScoped
public class RoleRestController2 implements RoleInternal2Api {

    @Override
    public Response searchRoles(RoleSearchCriteriaDTO roleSearchCriteriaDTO) {
        return Response.ok().build();
    }

}
