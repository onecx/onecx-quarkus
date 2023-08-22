package io.github.onecx.quarkus.parameter;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.restclient.runtime.QuarkusRestClientBuilder;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.common.AbstractConfigSource;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.mutiny.core.eventbus.EventBus;

@ApplicationScoped
public class ParametersService {
    private static final Logger log = LoggerFactory.getLogger(ParametersService.class);

    static Map<String, String> data = new ConcurrentHashMap<>();

    static ParametersConfigSource source = new ParametersConfigSource();

    ParameterRestClient client;

    @Inject
    EventBus bus;

    @Inject
    Vertx vertx;

    SmallRyeConfig config;

    SmallRyeConfig quarkusConfig;

    boolean metrics;

    /**
     * Initialize the cache and parameters client
     *
     * @param parametersConfig the parameters management configuration
     */
    public void init(ParametersConfig parametersConfig) {
        this.metrics = parametersConfig.metrics.enabled;
        // init rest client
        String applicationId = parametersConfig.applicationId.orElse("dummy-app");

        var clientBuilder = new QuarkusRestClientBuilder();
        client = clientBuilder
                .baseUri(URI.create(parametersConfig.host))
                .build(ParameterRestClient.class);

        // create custom config
        ConfigBuilder builder = ConfigProviderResolver.instance()
                .getBuilder()
                .addDiscoveredConverters()
                .withSources(source);
        config = (SmallRyeConfig) builder.build();
        quarkusConfig = (SmallRyeConfig) ConfigProvider.getConfig();

        // update parameters at start
        if (parametersConfig.updateAtStart) {
            update(applicationId)
                    .subscribe().with(d -> log.info("Init parameters cache: {}", d));
        }

        // setup scheduler for update
        vertx.setPeriodic(parametersConfig.updateIntervalInMilliseconds,
                id -> update(applicationId).subscribe().with(d -> log.info("Update parameters cache: {}", d)));
    }

    /**
     * Update the cache of the parameters
     */
    Uni<Map<String, String>> update(String applicationId) {
        return client.getParameters(applicationId)
                .onFailure().recoverWithItem(ex -> {
                    log.error("Error updating the configuration from parameters management. Error: {}", ex.getMessage());
                    return null;
                })
                .onItem().ifNotNull().invoke(m -> data.putAll(m));
    }

    /**
     * Parameters config source
     */
    public static class ParametersConfigSource extends AbstractConfigSource {

        public ParametersConfigSource() {
            super("onecx-parameters-config-source", 999);
        }

        @Override
        public Map<String, String> getProperties() {
            return ParametersService.data;
        }

        @Override
        public Set<String> getPropertyNames() {
            return ParametersService.data.keySet();
        }

        @Override
        public String getValue(String propertyName) {
            return ParametersService.data.get(propertyName);
        }

    }

    /**
     * Return the resolved property value with the specified type for the
     * specified property name from the underlying {@linkplain ConfigSource configuration sources}.
     * <p>
     * The configuration value is not guaranteed to be cached by the implementation, and may be expensive
     * to compute; therefore, if the returned value is intended to be frequently used, callers should consider storing
     * rather than recomputing it.
     *
     * @param <T>
     *        The property type
     * @param propertyName
     *        The configuration property name
     * @param propertyType
     *        The type into which the resolved property value should get converted
     * @param defaultValue
     *        The default value to return if the property value could not get resolved
     * @return the resolved property value as an instance of the requested type
     * @throws java.lang.IllegalArgumentException if the property cannot be converted to the specified type
     * @throws java.util.NoSuchElementException if the property isn't present in the configuration
     */
    public <T> T getValue(String propertyName, Class<T> propertyType, String defaultValue) {
        // check value from cache
        Optional<T> value = config.getOptionalValue(propertyName, propertyType);
        if (value.isPresent()) {
            return sendMetrics(propertyName, propertyType, defaultValue, config.getRawValue(propertyName), value.get());
        }

        // check value from quarkus configuration
        value = quarkusConfig.getOptionalValue(propertyName, propertyType);
        if (value.isPresent()) {
            return sendMetrics(propertyName, propertyType, defaultValue, quarkusConfig.getRawValue(propertyName), value.get());
        }

        // check default value
        if (defaultValue != null && !defaultValue.isBlank()) {
            return sendMetrics(propertyName, propertyType, defaultValue, defaultValue,
                    quarkusConfig.convert(defaultValue, propertyType));
        }

        // no default value return null
        return null;
    }

    private <T> T sendMetrics(String propertyName, Class<T> propertyType, String defaultValue, String rawValue,
            T currentValue) {
        if (metrics) {
            bus.send(ParameterEvent.NAME, ParameterEvent.of(propertyName, propertyType, defaultValue, rawValue));
        }
        return currentValue;
    }

    /**
     * Return the resolved property value with the specified type for the
     * specified property name from the underlying {@linkplain ConfigSource configuration sources}.
     * <p>
     * The configuration value is not guaranteed to be cached by the implementation, and may be expensive
     * to compute; therefore, if the returned value is intended to be frequently used, callers should consider storing
     * rather than recomputing it.
     * <p>
     * This is a shortcut to the {@link #getValue(String, Class, String) getValue} method using <code>null</code> as
     * <code>defaultValue</code>.
     *
     * @param <T>
     *        The property type
     * @param propertyName
     *        The configuration property name
     * @param propertyType
     *        The type into which the resolved property value should get converted
     * @return the resolved property value as an instance of the requested type
     * @throws java.lang.IllegalArgumentException if the property cannot be converted to the specified type
     * @throws java.util.NoSuchElementException if the property isn't present in the configuration
     */
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        return getValue(propertyName, propertyType, null);
    }
}
