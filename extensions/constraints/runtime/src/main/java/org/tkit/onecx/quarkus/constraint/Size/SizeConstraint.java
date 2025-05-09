package org.tkit.onecx.quarkus.constraint.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = SizeValidator.class)
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SizeConstraint {

    String min() default "1";

    String max() default "1000000";

    String key() default "";

    String message() default "Size Constraint Violation";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
