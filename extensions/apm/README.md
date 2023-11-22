# onecx-apm

```
<dependency>
    <groupId>io.github.onecx.quarkus</groupId>
    <artifactId>onecx-apm</artifactId>
</dependency>
```

### How to use

1. First you need to add the maven dependency shown at the top
2. Then you can use @PermissionsAllowed annotation

```
    @GET
    @Path("write")
    @PermissionsAllowed(value = "apm:resource#action")
    public Response adminWrite() {
        return Response.ok("OK").build();
    }
```

### Configuration

To use the extension you need to set the url to parameters management backend:

```properties
# disable or enable the extension  
onecx.apm.enabled=true
# application ID for the APM service 
onecx.apm.application-id=${quarkus.application.name}
# APM principal header parameter.
onecx.apm.token-header-param=apm-principal-token
# Name of the permission module. @PermissionsAllowed(value = "apm:resource1#admin-write")
onecx.apm.name=apm
# disable or enable debug log of permissions
onecx.apm.debug-log=false
# APM client version. Only v2, v3 version are supported
onecx.apm.client.version=v3
# Separator for resource and action. <resource>#<action>
onecx.apm.client.v3.context-action-separator=#
```

Rest client configuration property: `quarkus.rest-client.onecx-apm-svc`

```properties
quarkus.rest-client.onecx-apm.url=http://onecx-apm-svc:8080
```



