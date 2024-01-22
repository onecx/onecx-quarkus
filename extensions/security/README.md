# onecx-security

Onecx security extension.

Build time properties to map `@PermissionsAllow` annotation to implementation class from generated
interface.

```properties
# enable or disable mapping of the @PermissionAllowed from interface to implementation class
onecx.security.mapping-annotation.enabled=true
# mapping only the classes from packages
onecx.security.mapping-annotation.packages=io.github.onecx,gen.io.github.onecx
```