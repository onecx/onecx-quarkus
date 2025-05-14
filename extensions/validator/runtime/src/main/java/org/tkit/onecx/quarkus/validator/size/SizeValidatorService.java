package org.tkit.onecx.quarkus.validator.size;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.tkit.onecx.quarkus.validator.config.ValidatorConfig;
import org.tkit.onecx.quarkus.validator.parameters.SizeParameter;
import org.tkit.onecx.quarkus.validator.service.ValueService;

@ApplicationScoped
public class SizeValidatorService {

    @Inject
    ValidatorConfig config;

    @Inject
    Instance<ValueService> valueService;

    SizeValidatorResult initSizeParameter(String key, SizeParameter defaultSizeParameter) {
        SizeValidatorResult result = new SizeValidatorResult();
        result.key = key;
        result.defaultValue = defaultSizeParameter;

        if (!config.values().enabled()) {
            return result;
        }

        result.provider = valueService.get().getName();
        result.parameterName = config.values().mapping().getOrDefault(key, key);
        result.value = valueService.get().getValue(result.parameterName, SizeParameter.class, defaultSizeParameter);
        return result;
    }

    String getMessage(SizeValidatorResult result) {
        String template = config.size().template();
        return String.format(template, result.provider, result.key, result.parameterName, result.value.getMessage(),
                result.value.getMin(),
                result.value.getMax());
    }

    static class SizeValidatorResult {

        private String provider;

        private String key;

        private String parameterName;

        private SizeParameter defaultValue;

        private SizeParameter value;

        public String getKey() {
            return key;
        }

        public String getParameterName() {
            return parameterName;
        }

        public SizeParameter getDefaultValue() {
            return defaultValue;
        }

        public SizeParameter getValue() {
            return value;
        }

    }
}
