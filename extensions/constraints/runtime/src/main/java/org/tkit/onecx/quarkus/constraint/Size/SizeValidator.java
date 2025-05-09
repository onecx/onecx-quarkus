package org.tkit.onecx.quarkus.constraint.Size;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Dependent
public class SizeValidator implements ConstraintValidator<SizeConstraint, Integer> {

    SizeParameter defaultSizeParameter;

    @Inject
    Instance<SizeValidatorService> sizeValidatorService;

    @Override
    public void initialize(SizeConstraint constraintAnnotation) {
        this.defaultSizeParameter = new SizeParameter();
        defaultSizeParameter.setMax(Long.parseLong(constraintAnnotation.max()));
        defaultSizeParameter.setMin(Long.parseLong(constraintAnnotation.min()));
        defaultSizeParameter.setMessage(constraintAnnotation.message());
        defaultSizeParameter.setKey(constraintAnnotation.key());
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        var finalSizeParameter = sizeValidatorService.get().initSizeParameter(defaultSizeParameter);
        var errorMessage = sizeValidatorService.get().getFormattedErrorMessage(finalSizeParameter);
        if (value == null) {
            return true;
        }
        if (value > finalSizeParameter.getMax()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
            return false;
        }
        if (value < finalSizeParameter.getMin()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
            return false;
        }
        return true;
    }
}
