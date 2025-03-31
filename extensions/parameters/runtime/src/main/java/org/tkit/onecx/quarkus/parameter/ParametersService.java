package org.tkit.onecx.quarkus.parameter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.tkit.onecx.quarkus.parameter.config.ParametersConfig;
import org.tkit.onecx.quarkus.parameter.history.ParametersHistoryEvent;
import org.tkit.onecx.quarkus.parameter.mapper.ParametersValueMapper;
import org.tkit.onecx.quarkus.parameter.runtime.ParametersDataService;

import io.vertx.mutiny.core.eventbus.EventBus;

@ApplicationScoped
public class ParametersService {

    @Inject
    ParametersDataService dataService;

    @Inject
    EventBus bus;

    @Inject
    ParametersValueMapper mapper;

    @Inject
    ParametersConfig parametersConfig;

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
    public <T> T getValue(String name, Class<T> type, T defaultValue) {
        var value = defaultValue;
        var raw = dataService.getRawValue(name);
        if (raw != null) {
            value = mapper.toType(raw, type);
        }
        if (parametersConfig.history().enabled()) {
            bus.send(ParametersHistoryEvent.NAME, ParametersHistoryEvent.of(name, type, defaultValue, raw));
        }
        return value;
    }

    /**
     * Return the resolved property value with the specified type for the
     * specified property name from the underlying {@linkplain ConfigSource configuration sources}.
     * <p>
     * This is a shortcut to the {@link #getValue(String, Class, T) getValue} method using <code>null</code> as
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
}
