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

| Configuration property       | Default         | Description                                |
|------------------------------|-----------------|--------------------------------------------|
| onecx.apm.enabled            | true            | disable or enable the extension            |
| onecx.apm.application-id     | ${quarkus.name} | application ID for the APM service         |
| onecx.apm.api-version        | v3              | only v2, v3 version are supported          |
| onecx.apm.action-separator   | #               | only for v3 version <resource>#<action>    |
| onecx.apm.log                | false           | disable or enable debug log of permissions |

