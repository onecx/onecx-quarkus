package org.tkit.onecx.quarkus.constraint.Size;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.tkit.onecx.quarkus.constraint.config.ConstraintConfig;
import org.tkit.onecx.quarkus.parameter.ParametersService;

@ApplicationScoped
public class SizeValidatorService {

    @Inject
    ConstraintConfig config;

    @Inject
    ParametersService parametersService;

    /**
     * Method to overwrite
     */
    public SizeParameter initSizeParameter(SizeParameter defaultSizeParameter) {
        if (config.parameters().enabled() && defaultSizeParameter.getKey() != null
                && !defaultSizeParameter.getKey().isEmpty()) {
            String paramName = config.parameters().mapping().get(defaultSizeParameter.getKey()) != null
                    ? config.parameters().mapping().get(defaultSizeParameter.getKey())
                    : defaultSizeParameter.getKey();
            try {
                defaultSizeParameter = parametersService.getValue(paramName, SizeParameter.class, defaultSizeParameter);
            } catch (Exception exception) {
                // use defaults
            }
        }
        return defaultSizeParameter;
    }

    public String getFormattedErrorMessage(SizeParameter parameters) {
        String template = config.size().template();
        return String.format(template, config.size().prefix(), parameters.getMessage(), parameters.getMin(),
                parameters.getMax());
    }
}
