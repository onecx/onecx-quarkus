package org.tkit.onecx.quarkus.parameter.deployment.devservices;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ParametersDbDevServicesProviderBuildItem extends SimpleBuildItem {

    private final String username;
    private final String password;
    private final String jdbcUrl;

    public ParametersDbDevServicesProviderBuildItem(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
