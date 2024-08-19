package io.github.poshjosh.ratelimiter.raas.resources;

import io.github.poshjosh.ratelimiter.raas.exceptions.ExceptionMessage;
import io.github.poshjosh.ratelimiter.raas.exceptions.RaasException;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import io.github.poshjosh.ratelimiter.raas.services.RateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class RateResource {
    public static final String PATH = "/rates";
    private final RateService rateService;

    public RateResource(RateService rateService) {
        this.rateService = rateService;
    }

    @PostMapping(value = {PATH+"/tree", PATH+"/tree/"})
    public List<RatesDto> postLimits(@RequestBody Map<String, Object> rateTree)
            throws RaasException {
        log.debug("Posting rate tree: {}", rateTree);
        try {
            return rateService.addRateTree(rateTree);
        } catch (IllegalArgumentException e) {
            throw new RaasException(ExceptionMessage.BAD_REQUEST, e);
        }
    }

    @PostMapping(value = {PATH, PATH+"/"})
    public RatesDto postLimit(@RequestBody RatesDto ratesDto) throws RaasException {
        log.debug("Posting: {}", ratesDto);
        try {
            return rateService.addRates(ratesDto);
        } catch (IllegalArgumentException e) {
            throw new RaasException(ExceptionMessage.BAD_REQUEST, e);
        }
    }

    @GetMapping({PATH+"/{id}", PATH+"/{id}/"})
    public RatesDto getRates(@PathVariable String id) throws RaasException {
        log.debug("Getting rates for id: {}", id);
        return rateService.findRates(id)
                .orElseThrow(() -> new RaasException(ExceptionMessage.RATES_NOT_FOUND));
    }

    @DeleteMapping({PATH+"/{id}", PATH+"/{id}/"})
    public ResponseEntity<String> deleteLimit(@PathVariable String id) throws RaasException {
        log.debug("Deleting limit for id: {}", id);
        if(rateService.deleteRates(id).isEmpty()) {
            throw new RaasException(ExceptionMessage.RATES_NOT_FOUND);
        }
        return ResponseEntity.ok("OK");
    }
}
