# onecx-permission

```
<dependency>
    <groupId>org.tkit.onecx.quarkus</groupId>
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
onecx.permissions.enabled=true
# disable or enable cache for permission
onecx.permissions.cache-enabled=true
# application ID for the APM service 
onecx.permissions.application-id=${quarkus.application.name}
# Name of the permission module. @PermissionsAllowed(value = "onecx:resource1#admin-write")
onecx.permissions.name=onecx
# Header token for permissions. For example  Authorization or apm-principal-token header parameter
onecx.permissions.request-token-from-header-param=Authorization
# Separator for resource and action. <resource>#<action>
onecx.permissions.key-separator=#
# disable or enable mock data for permission
onecx.permissions.mock.enabled=false
# list of permissions for principal role
# for example: 
#  onecx.permission.mock.data.roles.role1.resource1=admin-write,read
#  onecx.permission.mock.data.roles.role2.resource2=read 
onecx.permissions.mock.data.roles.<role>.<resource>=<list-of-actions>
```



Rest client configuration property: `quarkus.rest-client.onecx-permission`

```properties
quarkus.rest-client.onecx-permissions.url=http://onecx-permission-svc:8080
```



