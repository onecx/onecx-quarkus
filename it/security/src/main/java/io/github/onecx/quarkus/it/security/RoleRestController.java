package io.github.onecx.quarkus.it.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import gen.io.github.onecx.quarkus.security.example.RoleInternalApi;
import gen.io.github.onecx.quarkus.security.example.model.CreateRoleRequestDTO;
import gen.io.github.onecx.quarkus.security.example.model.UpdateRoleRequestDTO;

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
    public Response updateRole(String id, UpdateRoleRequestDTO updateRoleRequestDTO) {
        return Response.ok().build();
    }
}
