package org.tkit.onecx.quarkus.validator.size;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.tkit.onecx.quarkus.validator.constraints.Size;
import org.tkit.onecx.quarkus.validator.parameters.SizeParameter;

@Dependent
public class SizeValidator implements ConstraintValidator<Size, Integer> {

    SizeParameter defaultSizeParameter;

    String key;

    @Inject
    Instance<SizeValidatorService> sizeValidatorService;

    @Override
    public void initialize(Size constraintAnnotation) {
        this.defaultSizeParameter = new SizeParameter();
        defaultSizeParameter.setMax(constraintAnnotation.max());
        defaultSizeParameter.setMin(constraintAnnotation.min());
        defaultSizeParameter.setMessage(constraintAnnotation.message());
        key = constraintAnnotation.key();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        var sizeValidatorResult = sizeValidatorService.get().initSizeParameter(key, defaultSizeParameter);
        if (value > sizeValidatorResult.getValue().getMax()) {
            return violation(sizeValidatorResult, context);
        }
        if (value < sizeValidatorResult.getValue().getMin()) {
            return violation(sizeValidatorResult, context);
        }
        return true;
    }

    private boolean violation(SizeValidatorService.SizeValidatorResult sizeValidatorResult,
            ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        var msg = sizeValidatorService.get().getMessage(sizeValidatorResult);
        context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
        return false;
    }
}
