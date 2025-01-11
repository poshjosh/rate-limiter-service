package io.github.poshjosh.ratelimiter.raas.resources;

import io.github.poshjosh.ratelimiter.raas.exceptions.ExceptionMessage;
import io.github.poshjosh.ratelimiter.raas.exceptions.RaasException;
import io.github.poshjosh.ratelimiter.raas.model.HttpRequestDto;
import io.github.poshjosh.ratelimiter.raas.model.LimitDto;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import io.github.poshjosh.ratelimiter.raas.services.PermitService;
import io.github.poshjosh.ratelimiter.raas.services.RateService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class PermitResource {
    public static final String PATH = "/permits";
    private final PermitService permitService;
    private final RateService rateService;

    public PermitResource(PermitService permitService, RateService rateService) {
        this.permitService = permitService;
        this.rateService = rateService;
    }

    @RequestMapping(value = {PATH+"/limit", PATH+"/limit/"})
    public ResponseEntity<Boolean> addAndAcquire(
            @Valid @RequestBody(required = false) LimitDto limitDto) throws
            RaasException {
        RatesDto ratesDto = limitDto.getLimit();
        rateService.addRatesIfNotExisting(ratesDto);
        return tryToAcquire(ratesDto.getId(), limitDto.getPermits(),
                limitDto.isAsync(), limitDto.getRequest());
    }

    @RequestMapping(
            path={PATH+"/acquire", PATH+"/acquire/"},
            method={ RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.GET })
    public ResponseEntity<Boolean> tryToAcquire(
            @RequestParam("rateId") String rateId,
            @RequestParam(name = "permits", defaultValue = "1") int permits,
            @RequestParam(name = "async", defaultValue = "false") boolean async,
            @Valid @RequestBody(required = false) HttpRequestDto httpRequestDto) throws RaasException {
        if (async) {
            log.debug("Trying to async acquire {} permits from rate: {} for: {}",
                    permits, rateId, httpRequestDto);
            final Boolean result = isAvailable(rateId, httpRequestDto);
            permitService.tryAcquireAsync(rateId, permits, httpRequestDto);
            return toResponse(result);
        }
        log.debug("Trying to acquire {} permits from rate: {} for: {}",
                permits, rateId, httpRequestDto);
        return toResponse(permitService.tryAcquire(rateId, permits, httpRequestDto));
    }

    private ResponseEntity<Boolean> toResponse(boolean success) throws RaasException {
        if (success) {
            return ResponseEntity.ok(Boolean.TRUE);
        }
        throw new RaasException(ExceptionMessage.TOO_MANY_REQUESTS);
    }

    @RequestMapping(
            path={PATH+"/available", PATH+"/available/"},
            method={ RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.GET })
    public Boolean isAvailable(
            @RequestParam("rateId") String rateId,
            @Valid @RequestBody(required = false) HttpRequestDto httpRequestDto) {
        log.debug("Checking if permits available for rate: {} for: {}", rateId, httpRequestDto);
        return permitService.isAvailable(rateId, httpRequestDto);
    }
}
