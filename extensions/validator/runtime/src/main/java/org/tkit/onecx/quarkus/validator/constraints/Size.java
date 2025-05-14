package org.tkit.onecx.quarkus.validator.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.tkit.onecx.quarkus.validator.size.SizeValidator;

/**
 * Size constraints
 */
@Constraint(validatedBy = SizeValidator.class)
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Size {

    /**
     * Minimum value.
     */
    long min();

    /**
     * Maximum value.
     */
    long max();

    /**
     * Unique key that represents the constraints in the application.
     */
    String key();

    /**
     * Constraints error message
     */
    String message() default "Size Constraint Violation";

    Class<? extends Payload>[] payload() default {};

    Class<?>[] groups() default {};

}
