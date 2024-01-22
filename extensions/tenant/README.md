# onecx-tenant

```xml
<dependency>
    <groupId>io.github.onecx.quarkus</groupId>
    <artifactId>onecx-tenant</artifactId>
</dependency>
```

Implementation of the [tkit-quarkus-rest-context](https://github.com/1000kit/tkit-quarkus/blob/main/extensions/rest-context/) tenant resolver service.

To activate multi-tenancy for hibernate, add the following maven dependency to your project [tkit-quarkus-jpa-tenant](https://github.com/1000kit/tkit-quarkus/tree/main/extensions/jpa-tenant).

```xml
<dependency>
    <groupId>org.tkit.quarkus.lib</groupId>
    <artifactId>tkit-quarkus-jpa-tenant</artifactId>
</dependency>
```



### Configuration

```properties
# Enable or disable cache tenant id for token
onecx.tenant.cache-enabled=true
```

Rest client configuration property: `quarkus.rest-client.onecx-tenant-svc`

```properties
quarkus.rest-client.onecx_tenant.url=http://onecx-tenant-svc:8080
```