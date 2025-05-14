package org.tkit.onecx.quarkus.validator.service;

import jakarta.inject.Inject;

import org.tkit.onecx.quarkus.parameter.ParametersService;

public class ParameterValueService implements ValueService {

    @Inject
    ParametersService parametersService;

    @Override
    public String getName() {
        return "onecx-parameters";
    }

    @Override
    public <T> T getValue(String name, Class<T> type, T defaultValue) {
        return parametersService.getValue(name, type, defaultValue);
    }
}
