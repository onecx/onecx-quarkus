package org.tkit.onecx.quarkus.validator.size;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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

        var service = sizeValidatorService.get();
        var sizeValidatorResult = service.initSizeParameter(key, defaultSizeParameter);
        if (value > sizeValidatorResult.getValue().getMax()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(sizeValidatorResult.getMessage()).addConstraintViolation();
            return false;
        }
        if (value < sizeValidatorResult.getValue().getMin()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(sizeValidatorResult.getMessage()).addConstraintViolation();
            return false;
        }
        return true;
    }
}
