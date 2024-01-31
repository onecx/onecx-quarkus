# onecx-openapi-generator

 openapi generator template extension for the onecx

```xml
<dependency>
    <groupId>org.tkit.onecx.quarkus</groupId>
    <artifactId>onecx-openapi-generator</artifactId>
</dependency>
```

## Configuration

Add this library in to the `openapi-generator-maven-plugin` class-path.

```xml
    <plugins>
        <plugin>
            <groupId>org.openapitools</groupId>
            <artifactId>openapi-generator-maven-plugin</artifactId>
                <dependencies>
                    <dependency>
                        <groupId>org.tkit.onecx.quarkus</groupId>
                        <artifactId>onecx-openapi-generator</artifactId>
                    </dependency>
                </dependencies>
            </plugin>
    </plugins>
```

### Additional properties 

#### OpenApi onecx permissions format

```yaml
x-onecx:
  # list of permissions
  permissions:
```

Example 

```yaml
openapi: 3.0.3
info:
  title: example
  version: 1.0.0
servers:
  - url: "http://example"
paths:
  /internal/roles:
    post:
      x-onecx:
        permissions:
          role:
            - read
            - write
            - all
      description: Create new role
      operationId: createRole
      requestBody:
        ...
```
Generate `@PermissionsAllowed` for `onecx` permissions.

```xml
  <plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <configuration>
       <additionalProperties>onecx-permissions=true</additionalProperties>
       ...
    </configuration>
  </plugin>
```

Generate `@PermissionsAllowed` for `oauth` scopes.

```xml
  <plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <configuration>
       <additionalProperties>onecx-scopes=true</additionalProperties>
       ...
    </configuration>
  </plugin>
```
