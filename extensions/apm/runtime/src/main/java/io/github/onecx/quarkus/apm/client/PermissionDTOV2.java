package io.github.onecx.quarkus.apm.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
public class PermissionDTOV2 {

    @JsonProperty(value = "action", required = false)
    private String action = null;

    @JsonProperty(value = "key", required = false)
    private String key = null;

    @JsonProperty(value = "name", required = false)
    private String name = null;

    @JsonProperty(value = "resource", required = false)
    private String resource = null;

    public String getAction() {
        return action;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return "PermissionDTO{" +
                "action='" + action + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                '}';
    }
}
