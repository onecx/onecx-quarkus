package org.tkit.onecx.quarkus.validator.size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = SizeValidator.class)
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Size {

    long min();

    long max();

    String key();

    String message() default "Size Constraint Violation";

    Class<? extends Payload>[] payload() default {};

    Class<?>[] groups() default {};

}
