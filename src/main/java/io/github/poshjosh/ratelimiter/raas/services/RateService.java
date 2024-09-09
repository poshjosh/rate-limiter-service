package io.github.poshjosh.ratelimiter.raas.services;

import io.github.poshjosh.ratelimiter.matcher.MatchContext;
import io.github.poshjosh.ratelimiter.RateLimiterRegistry;
import io.github.poshjosh.ratelimiter.model.Rates;
import io.github.poshjosh.ratelimiter.raas.exceptions.RaasException;
import io.github.poshjosh.ratelimiter.raas.model.*;
import io.github.poshjosh.ratelimiter.raas.cache.RedisRatesCache;
import io.github.poshjosh.ratelimiter.web.core.RequestInfo;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.*;

@Slf4j
@Service
public class RateService {

    private final RateMapper rateMapper;
    private final RedisRatesCache ratesCache;
    private final LocalValidatorFactoryBean localValidatorFactoryBean;
    private final RateLimiterRegistry<RequestInfo> rateLimiterRegistry;

    public RateService(
            RateMapper rateMapper,
            RedisRatesCache ratesCache,
            LocalValidatorFactoryBean localValidatorFactoryBean,
            RateLimiterRegistry<RequestInfo> rateLimiterRegistry) {
        this.rateMapper = rateMapper;
        this.ratesCache = ratesCache;
        this.localValidatorFactoryBean = localValidatorFactoryBean;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    public List<RatesDto> addRateTree(Map<String, Object> limitTree)
            throws RaasException, ConstraintViolationException {
        final List<RatesDto> rates = rateMapper.toDtos(limitTree);
        for(RatesDto rate : rates) {
            var violations = localValidatorFactoryBean.validate(rate);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            this.addRates(rate);
        }
        return rates;
    }

    public RatesDto addRates(RatesDto ratesDto) {
        final String id = ratesDto.getId();
        if (rateLimiterRegistry.isRegistered(id)) {
            rateLimiterRegistry.deregister(id);
            // remove from cache, is not needed because our put below
            // basically replaces any old value
        }
        final Rates rates = rateMapper.toEntity(ratesDto);
        rateLimiterRegistry.register(rates);
        ratesCache.put(id, rates);
        log.debug("Added: {}", rates);
        return ratesDto;
    }

    public Optional<RatesDto> deleteRates(String id) {
        final Optional<RatesDto> result = findRates(id);
        rateLimiterRegistry.deregister(id);
        ratesCache.remove(id);
        log.debug("Removed: {} = {}", id, result);
        return result;
    }

    public Optional<RatesDto> findRates(String id) {
        final Optional<RatesDto> result = rateLimiterRegistry.getMatchContextOptional(id)
                .map(MatchContext::getRates)
                .map(rateMapper::toDto);
        log.debug("Found: {} = {}", id, result);
        return result;
    }
}
