package org.tkit.onecx.quarkus.parameter.mapper;

import org.tkit.onecx.quarkus.parameter.ConvertValueException;

public interface ParametersValueMapper {

    <T> T toType(final Object value, final Class<T> clazz) throws ConvertValueException;

    Object toMap(final String value) throws ConvertValueException;

}
