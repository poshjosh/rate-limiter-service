package io.github.poshjosh.ratelimiter.raas.resources;

import io.github.poshjosh.ratelimiter.raas.model.HttpRequestDto;
import io.github.poshjosh.ratelimiter.raas.services.PermitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class PermitResource {
    public static final String PATH = "/permits";
    private final PermitService permitService;

    public PermitResource(PermitService permitService) {
        this.permitService = permitService;
    }

    @RequestMapping(
            path={PATH+"/acquire", PATH+"/acquire/"},
            method={ RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST, RequestMethod.GET })
    public Boolean tryToAcquire(
            @RequestParam("rateId") String rateId,
            @RequestParam(name = "permits", defaultValue = "1") int permits,
            @RequestParam(name = "async", defaultValue = "false") boolean async,
            // TODO - Add validation
            @RequestBody(required = false) HttpRequestDto httpRequestDto) {
        if (async) {
            log.debug("Trying to async acquire {} permits from rate: {} for: {}",
                    permits, rateId, httpRequestDto);
            final Boolean result = isAvailable(rateId, httpRequestDto);
            if (httpRequestDto == null) {
                permitService.tryAcquireAsync(rateId, permits);
                return result;
            }
            permitService.tryAcquireAsync(rateId, permits, httpRequestDto);
            return result;
        }
        log.debug("Trying to acquire {} permits from rate: {} for: {}",
                permits, rateId, httpRequestDto);
        if (httpRequestDto == null) {
            return permitService.tryAcquire(rateId, permits);
        }
        return permitService.tryAcquire(rateId, permits, httpRequestDto);
    }

    @RequestMapping(
            path={PATH+"/available", PATH+"/available/"},
            method={ RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST, RequestMethod.GET })
    public Boolean isAvailable(
            @RequestParam("rateId") String rateId,
            // TODO - Add validation
            @RequestBody(required = false) HttpRequestDto httpRequestDto) {
        log.debug("Checking if permits available for rate: {} for: {}", rateId, httpRequestDto);
        if (httpRequestDto == null) {
            return permitService.isAvailable(rateId);
        }
        return permitService.isAvailable(rateId, httpRequestDto);
    }
}
