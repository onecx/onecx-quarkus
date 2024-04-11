package org.tkit.onecx.quarkus.permission.service;

import java.util.List;

public class PermissionResponse {

    private List<String> actions;

    public static PermissionResponse create(List<String> data) {
        var r = new PermissionResponse();
        r.actions = data;
        return r;
    }

    public List<String> getActions() {
        return actions;
    }

}
