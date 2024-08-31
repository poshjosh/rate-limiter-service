package io.github.poshjosh.ratelimiter.raas.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = JavaClassValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface JavaClassConstraint {
    String message() default "Could not load class";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}