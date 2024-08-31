package io.github.poshjosh.ratelimiter.raas.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RatesOperatorValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RatesOperatorConstraint {
    String message() default "required.operator";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}