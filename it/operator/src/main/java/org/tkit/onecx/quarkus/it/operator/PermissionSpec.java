package org.tkit.onecx.quarkus.it.operator;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionSpec {

    @JsonProperty(value = "appId", required = true)
    private String appId;

    @JsonProperty(value = "productName", required = true)
    private String productName;

    @JsonProperty(value = "name", required = true)
    private String name;

    @JsonProperty(value = "description", required = true)
    private String description;

    @JsonProperty("permissions")
    private Map<String, Map<String, String>> permissions;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Map<String, String>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Map<String, String>> permissions) {
        this.permissions = permissions;
    }

}
