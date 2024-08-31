package io.github.poshjosh.ratelimiter.raas.model.validation;

import io.github.poshjosh.ratelimiter.raas.model.Operator;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RatesOperatorValidator implements ConstraintValidator<RatesOperatorConstraint, Object> {
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        final RatesDto ratesDto = (RatesDto)value;
        final Operator operator = ratesDto.getOperator();
        final int rateCount = ratesDto.getRates() == null ? 0 : ratesDto.getRates().size();
        return (operator != null && operator != Operator.NONE) || rateCount <= 1;
    }
}