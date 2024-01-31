package org.tkit.onecx.quarkus.parameter.deployment.devservices;

import io.quarkus.builder.item.SimpleBuildItem;

public final class ParametersDbDevServicesProviderBuildItem extends SimpleBuildItem {

    public String username;
    public String password;
    public String jdbcUrl;

    public ParametersDbDevServicesProviderBuildItem(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }
}
