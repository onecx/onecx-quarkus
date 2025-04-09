package org.tkit.onecx.quarkus.parameter.runtime;

import java.lang.annotation.Annotation;

import jakarta.enterprise.inject.spi.InjectionPoint;

import org.tkit.onecx.quarkus.parameter.Parameter;
import org.tkit.onecx.quarkus.parameter.ParametersService;

public class AbstractParameterProducer {

    protected Object getParameter(InjectionPoint injectionPoint, Class<?> clazz, ParametersService service) {
        Parameter param = getParameterAnno(injectionPoint);
        return service.getValue(param.name(), clazz);
    }

    protected Parameter getParameterAnno(InjectionPoint injectionPoint) {
        Parameter ft = null;
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(Parameter.class)) {
                ft = (Parameter) qualifier;
                break;
            }
        }
        return ft;
    }
}
