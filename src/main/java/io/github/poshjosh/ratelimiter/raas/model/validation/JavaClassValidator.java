package io.github.poshjosh.ratelimiter.raas.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class JavaClassValidator implements ConstraintValidator<JavaClassConstraint, String> {
    @Override
    public boolean isValid(String factoryClass, ConstraintValidatorContext cxt) {
        if (factoryClass != null && !factoryClass.isBlank()) {
            try {
                Class.forName(factoryClass);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return true;
    }
}