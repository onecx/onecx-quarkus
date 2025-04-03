package org.tkit.onecx.quarkus.parameter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.onecx.quarkus.parameter.mapper.ParametersValueMapper;
import org.tkit.onecx.quarkus.parameter.runtime.ParametersDataService;

@ApplicationScoped
public class ParametersService {

    @Inject
    ParametersDataService dataService;

    @Inject
    ParametersValueMapper mapper;

    /**
     * Return the resolved parameter value with the specified type.
     *
     * @param <T>
     *        The parameter type
     * @param name
     *        The parameter name
     * @param type
     *        The type into which the resolved parameter value should get converted
     * @param defaultValue
     *        The default parameter value to return if the parameter value could not get resolved
     *
     * @return the resolved parameter value as an instance of the requested type
     *
     * @throws org.tkit.onecx.quarkus.parameter.UpdateException if the update of the parameter failed and property
     *         <code>onecx.parameters.throw-update-exception</code> is set to <code>true</code>.
     * @throws org.tkit.onecx.quarkus.parameter.ConvertValueException if the parameter value cannot be converted to the
     *         specified type
     * @throws org.tkit.onecx.quarkus.parameter.TenantException if you enable multi-tenancy and <code>ApplicationContext</code>
     *         is null.
     */
    public <T> T getValue(String name, Class<T> type, T defaultValue) {
        var value = defaultValue;
        var raw = dataService.getRawValue(name);
        if (raw != null) {
            value = mapper.toType(raw, type);
        }
        dataService.addHistory(name, type, value, defaultValue);
        return value;
    }

    /**
     * Return the resolved parameter value with the specified type.
     * <p>
     * This is a shortcut to the {@link #getValue(String, Class, T) getValue} method using <code>null</code> as
     * <code>defaultValue</code>.
     *
     * @param <T>
     *        The parameter type
     * @param name
     *        The parameter name
     * @param type
     *        The type into which the resolved parameter value should get converted
     *
     * @return the resolved parameter value as an instance of the requested type
     * @throws org.tkit.onecx.quarkus.parameter.UpdateException if the update of the parameter failed and property
     *         <code>onecx.parameters.throw-update-exception</code> is set to <code>true</code>.
     * @throws org.tkit.onecx.quarkus.parameter.ConvertValueException if the parameter value cannot be converted to the
     *         specified type
     */
    public <T> T getValue(String name, Class<T> type) {
        return getValue(name, type, null);
    }
}
