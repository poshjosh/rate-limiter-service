package io.github.poshjosh.ratelimiter.raas.services;

import io.github.poshjosh.ratelimiter.MatchContext;
import io.github.poshjosh.ratelimiter.RateLimiterRegistry;
import io.github.poshjosh.ratelimiter.model.Rates;
import io.github.poshjosh.ratelimiter.raas.model.*;
import io.github.poshjosh.ratelimiter.raas.redis.RedisRatesCache;
import io.github.poshjosh.ratelimiter.web.core.RequestInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class RateService {

    private final RateMapper rateMapper;
    private final RedisRatesCache ratesCache;
    private final RateLimiterRegistry<RequestInfo> rateLimiterRegistry;

    public RateService(
            RateMapper rateMapper,
            RedisRatesCache ratesCache,
            RateLimiterRegistry<RequestInfo> rateLimiterRegistry) {
        this.rateMapper = rateMapper;
        this.ratesCache = ratesCache;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    public List<RatesDto> addRateTree(Map<String, Object> limitTree) throws IllegalArgumentException {
        final List<RatesDto> rates = rateMapper.toDtos(limitTree);
        rates.forEach(this::addRates);
        return rates;
    }

    public RatesDto addRates(RatesDto ratesDto) {
        final String id = ratesDto.getId();
        if (rateLimiterRegistry.isRegistered(id)) {
            rateLimiterRegistry.deregister(id);
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
