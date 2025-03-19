package org.tkit.onecx.quarkus.parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.parameter.client.ParameterClientService;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.mapper.ParameterValueMapper;
import org.tkit.onecx.quarkus.parameter.metrics.ParameterEvent;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.mutiny.core.eventbus.EventBus;

@ApplicationScoped
public class ParametersService {

    private static final Logger log = LoggerFactory.getLogger(ParametersService.class);

    static Map<String, Object> data = new ConcurrentHashMap<>();

    @Inject
    ParameterClientService client;

    @Inject
    EventBus bus;

    @Inject
    Vertx vertx;

    @Inject
    ParameterValueMapper mapper;

    boolean metrics;

    SmallRyeConfig config;

    // cache data of the parameters
    static Map<String, Object> parameterData = new HashMap<>();

    /**
     * Initialize the cache and parameters client
     *
     * @param parametersConfig the parameters management configuration
     */
    public void init(ParametersConfig parametersConfig) {
        this.metrics = parametersConfig.metrics().enabled();
        parametersConfig.parameters().forEach((k, v) -> parameterData.put(k, mapper.toMap(v.value().orElse(null))));

        config = (SmallRyeConfig) ConfigProvider.getConfig();

        // update parameters at start
        if (parametersConfig.updateAtStart()) {
            update()
                    .subscribe().with(d -> log.info("Init parameters cache: {}", d));
        }

        // setup scheduler for update
        vertx.setPeriodic(parametersConfig.updateIntervalInMilliseconds(),
                id -> update().subscribe().with(d -> log.info("Update parameters cache: {}", d)));
    }

    /**
     * Update the cache of the parameters
     */
    Uni<Map<String, Object>> update() {
        return client.getApplicationParameters()
                .onFailure().recoverWithItem(ex -> {
                    log.error("Error updating the configuration from parameters management. Error: {}", ex.getMessage());
                    return null;
                })
                .onItem().ifNotNull().invoke(m -> data.putAll(m));
    }

    /**
     * Return the resolved property value with the specified type for the
     * specified property name from the underlying {@linkplain ConfigSource configuration sources}.
     *
     * @param <T>
     *        The property type
     * @param name
     *        The configuration property name
     * @param type
     *        The type into which the resolved property value should get converted
     * @param defaultValue
     *        The default value to return if the property value could not get resolved
     * @return the resolved property value as an instance of the requested type
     * @throws java.lang.IllegalArgumentException if the property cannot be converted to the specified type
     * @throws java.util.NoSuchElementException if the property isn't present in the configuration
     */
    public <T> T getValue(String name, Class<T> type, String defaultValue) {
        var raw = getRawValue(name, defaultValue);
        var value = getObjectValue(raw, type);
        if (metrics) {
            bus.send(ParameterEvent.NAME, ParameterEvent.of(name, type, defaultValue, raw));
        }
        return value;
    }

    /**
     * Return the resolved property value with the specified type for the
     * specified property name from the underlying {@linkplain ConfigSource configuration sources}.
     * <p>
     * This is a shortcut to the {@link #getValue(String, Class, String) getValue} method using <code>null</code> as
     * <code>defaultValue</code>.
     *
     * @param <T>
     *        The property type
     * @param name
     *        The configuration property name
     * @param type
     *        The type into which the resolved property value should get converted
     * @return the resolved property value as an instance of the requested type
     * @throws java.lang.IllegalArgumentException if the property cannot be converted to the specified type
     * @throws java.util.NoSuchElementException if the property isn't present in the configuration
     */
    public <T> T getValue(String name, Class<T> type) {
        return getValue(name, type, null);
    }

    private <T> T getObjectValue(Object raw, Class<T> type) {
        if (raw == null) {
            return null;
        }
        return mapper.toType(raw, type);
    }

    private Object getRawValue(String name, String defaultValue) {

        // parameter value from cache
        var raw = data.get(name);
        if (raw != null) {
            return raw;
        }

        // parameter value from quarkus configuration
        var param = parameterData.get(name);
        if (param != null) {
            return param;
        }

        // parameter default value
        return defaultValue;
    }
}
