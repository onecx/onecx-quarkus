# onecx-parameter

```
<dependency>
    <groupId>io.github.onecx.quarkus</groupId>
    <artifactId>onecx-parameter</artifactId>
</dependency>
```

### How to use

1. First you need to add the maven dependency shown at the top
2. Then you can inject the ParametersService:

```
@Inject ParametersService parametersService;
```

3. After that you can use the `getValue` Method:

```
String value = parametersService.getValue("key", String.class, "DEFAULT_VALUE");
```

You retrieve either the value from the...
1. Parameter-Service
2. Project application.properties
3. Default Value

in this respective order.

So if you want to retrieve a value from the parameter-service/application.properties and throw an exception if it was not configured there, be sure to set the default-value to null or something else and check if a valid value was returned.

### Configuration

To use the extension you need to set the url to parameters management backend:

| Configuration property                                 | Default        |
|--------------------------------------------------------|----------------|
| onecx.parameters.enabled                               | true           |
| onecx.parameters.updateIntervalInMilliseconds          | 30000          |
| onecx.parameters.updateAtStart                         | false          |
| onecx.parameters.applicationId                         | <app name>     |
| onecx.parameters.instanceId                            | <instance id>  |
| onecx.parameters.metrics.enabled                       | true           |
| onecx.parameters.metrics.metricsIntervalInMilliseconds | 20000          |

