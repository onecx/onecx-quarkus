package org.tkit.onecx.quarkus.parameter.deployment.devservices;

import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class DevServicesConfig {

    /**
     * If DevServices has been explicitly enabled or disabled. DevServices is generally enabled
     * by default, unless there is an existing configuration present.
     * <p>
     * When DevServices is enabled Quarkus will attempt to automatically configure and start
     * a database when running in Dev or Test mode and when Docker is running.
     */
    @ConfigItem(name = "enabled", defaultValue = "true")
    public boolean enabled;

    /**
     * Enabled or disable log of the mock-server
     */
    @ConfigItem(name = "log", defaultValue = "false")
    public boolean log;

    /**
     * The container image name to use, for container based DevServices providers.
     */
    @ConfigItem(name = "image-name")
    public Optional<String> imageName;

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @ConfigItem(name = "port")
    public OptionalInt port;

    /**
     * Indicates if the MockServer server managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for MockServer starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-mockserver} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @ConfigItem(name = "shared", defaultValue = "true")
    public boolean shared;

    /**
     * The value of the {@code onecx-dev-service-parametersr} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for Mockserver looks for a container with the
     * {@code onecx-dev-service-parameters} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise, it
     * starts a new container with the {@code onecx-dev-service-parameters} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared MockServer servers.
     */
    @ConfigItem(name = "service-name", defaultValue = "onecx-parameters")
    public String serviceName;

    /**
     * Helper to define the stop strategy for containers created by DevServices.
     * In particular, we don't want to actually stop the containers when they
     * have been flagged for reuse, and when the Testcontainers configuration
     * has been explicitly set to allow container reuse.
     * To enable reuse, ass {@literal testcontainers.reuse.enable=true} in your
     * {@literal .testcontainers.properties} file, to be stored in your home.
     *
     * @see <a href="https://www.testcontainers.org/features/configuration/">Testcontainers Configuration</a>.
     */
    @ConfigItem(name = "reuse", defaultValue = "false")
    public boolean reuse;

    /**
     * The import data from file during the start.
     */
    @ConfigItem(name = "import-file")
    public Optional<String> importFile;

    /**
     * Parameters database dev service
     */
    @ConfigItem(name = "db")
    public ParametersDatabaseConfig db = new ParametersDatabaseConfig();

    /**
     * Parameters database config
     */
    @ConfigGroup
    public static class ParametersDatabaseConfig {

        /**
         * The container image name to use, for unleash database.
         */
        @ConfigItem(name = "image-name", defaultValue = "postgres:15.2")
        public String imageName;

        /**
         * The value of the {@code onecx-dev-service-parameters-db} label attached to the started container.
         * This property is used when {@code shared} is set to {@code true}.
         * In this case, before starting a container, Dev Services for Unleash DB looks for a container with the
         * {@code onecx-dev-service-parameters-db} label
         * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise, it
         * starts a new container with the {@code onecx-dev-service-parameters-db} label set to the specified value.
         * <p>
         * This property is used when you need multiple shared Unleash DB servers.
         */
        @ConfigItem(name = "service-name", defaultValue = "parameters-db")
        public String serviceName;

    }

}
