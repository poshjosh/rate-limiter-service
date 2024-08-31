package io.github.poshjosh.ratelimiter.raas.services;

import io.github.poshjosh.ratelimiter.RateLimiterRegistry;
import io.github.poshjosh.ratelimiter.raas.RateLimiterConfiguration;
import io.github.poshjosh.ratelimiter.raas.model.HttpRequestDto;
import io.github.poshjosh.ratelimiter.raas.model.HttpRequestMapper;
import io.github.poshjosh.ratelimiter.web.core.RequestInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class PermitService {
    private final HttpRequestMapper httpRequestMapper;

    private final RateLimiterRegistry<RequestInfo> rateLimiterRegistry;

    public PermitService(
            HttpRequestMapper httpRequestMapper,
            RateLimiterRegistry<RequestInfo> rateLimiterRegistry) {
        this.httpRequestMapper = httpRequestMapper;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Async
    public void tryAcquireAsync(String rateId, int permits, HttpRequestDto httpRequestDto) {
        tryAcquire(rateId, permits, httpRequestDto);
    }

    public boolean tryAcquire(String rateId, int permits, HttpRequestDto httpRequestDto) {
        addRateIdHeaderValue(httpRequestDto, rateId);
        final RequestInfo requestInfo = httpRequestMapper.toRequestInfo(httpRequestDto);
        final boolean acquired = rateLimiterRegistry.tryAcquire(requestInfo, permits);
        log.debug("Acquired {}, {} permits from rate: {} for {}",
                acquired, permits, rateId, httpRequestDto);
        return acquired;
    }

    public boolean isAvailable(String rateId, HttpRequestDto httpRequestDto) {
        addRateIdHeaderValue(httpRequestDto, rateId);
        final RequestInfo requestInfo = httpRequestMapper.toRequestInfo(httpRequestDto);
        final boolean available = rateLimiterRegistry.isWithinLimit(requestInfo);
        log.debug("Permit available {}, rate: {}, for {}", available, rateId, httpRequestDto);
        return available;
    }

    private void addRateIdHeaderValue(HttpRequestDto httpRequestDto, String value) {
        Map<String, List<String>> headers = httpRequestDto.getHeaders() == null
                || httpRequestDto.getHeaders().isEmpty()
                ? new HashMap<>() : new HashMap<>(httpRequestDto.getHeaders());
        headers.put(RateLimiterConfiguration.RATE_ID_HEADER, List.of(value));
        httpRequestDto.setHeaders(headers);
    }
}
