package org.tkit.onecx.quarkus.it.permision;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import gen.org.tkit.onecx.quarkus.permission.example.RoleInternalApi;
import gen.org.tkit.onecx.quarkus.permission.example.model.CreateRoleRequestDTO;
import gen.org.tkit.onecx.quarkus.permission.example.model.RoleSearchCriteriaDTO;
import gen.org.tkit.onecx.quarkus.permission.example.model.UpdateRoleRequestDTO;

@ApplicationScoped
public class RoleRestController implements RoleInternalApi {

    @Override
    public Response createRole(CreateRoleRequestDTO createRoleRequestDTO) {
        return Response.ok().build();
    }

    @Override
    public Response deleteRole(String id) {
        return Response.ok().build();
    }

    @Override
    public Response getRoleById(String id) {
        return Response.ok().build();
    }

    @Override
    public Response searchRoles(RoleSearchCriteriaDTO roleSearchCriteriaDTO) {
        return Response.ok().build();
    }

    @Override
    public Response updateRole(String id, UpdateRoleRequestDTO updateRoleRequestDTO) {
        return Response.ok().build();
    }
}
