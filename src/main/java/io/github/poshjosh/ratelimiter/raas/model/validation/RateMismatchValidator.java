package io.github.poshjosh.ratelimiter.raas.model.validation;

import io.github.poshjosh.ratelimiter.raas.model.RateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RateMismatchValidator implements ConstraintValidator<RatesOperatorConstraint, Object> {
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        final RateDto rateDto = (RateDto)value;
        final boolean hasRate = rateDto.getRate() != null && !rateDto.getRate().isBlank();
        final boolean hasPermits = rateDto.getPermits() > 0;
        if (hasRate) {
            return !hasPermits;
        } else {
            return hasPermits;
        }
    }
}