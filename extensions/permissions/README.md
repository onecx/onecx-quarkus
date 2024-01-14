# onecx-permission

```
<dependency>
    <groupId>io.github.onecx.quarkus</groupId>
    <artifactId>onecx-permission</artifactId>
</dependency>
```

### How to use

1. First you need to add the maven dependency shown at the top
2. Then you can use @PermissionsAllowed annotation

```
    @GET
    @Path("write")
    @PermissionsAllowed(value = "onecx:resource#action")
    public Response adminWrite() {
        return Response.ok("OK").build();
    }
```

### Configuration

To use the extension you need to set the url to parameters management backend:

```properties
# disable or enable the extension  
onecx.permission.enabled=true
# application ID for the APM service 
onecx.permission.application-id=${quarkus.application.name}
# APM principal header parameter.
onecx.permission.token-header-param=apm-principal-token
# Name of the permission module. @PermissionsAllowed(value = "onecx:resource1#admin-write")
onecx.permission.name=onecx
# Separator for resource and action. <resource>#<action>
onecx.permission.key-separator=#
# disable or enable mock data for permission
onecx.permission.mock.enabled=false
# list of permissions for principal role
# for example: 
#  onecx.permission.mock.data.role1.resources.resource1=admin-write,read
#  onecx.permission.mock.data.role2.resources.resource2=read 
onecx.permission.mock.data.<role>.resources.<action>=<list-of-permissions>
```

Rest client configuration property: `quarkus.rest-client.onecx-permission`

```properties
quarkus.rest-client.onecx-permissions.url=http://onecx-permission-svc:8080
```



