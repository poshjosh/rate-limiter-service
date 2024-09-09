package io.github.poshjosh.ratelimiter.raas.resources;

import io.github.poshjosh.ratelimiter.raas.exceptions.ExceptionMessage;
import io.github.poshjosh.ratelimiter.raas.exceptions.RaasException;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import io.github.poshjosh.ratelimiter.raas.services.RateService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class RateResource {
    public static final String PATH = "/rates";
    private final RateService rateService;

    public RateResource(RateService rateService) {
        this.rateService = rateService;
    }

    @PostMapping(value = {PATH+"/tree", PATH+"/tree/"})
    public ResponseEntity<List<RatesDto>> postRates(@Valid @RequestBody Map<String, Object> rateTree)
            throws RaasException, ConstraintViolationException {
        log.debug("Posting rate tree: {}", rateTree);
        List<RatesDto> ratesDto = rateService.addRateTree(rateTree);
        String ids = ratesDto.stream().map(RatesDto::getId).collect(Collectors.joining(","));
        return ResponseEntity.created(URI.create(PATH + "/tree/" + ids)).body(ratesDto);
    }

    @PostMapping(value = {PATH, PATH+"/"})
    public ResponseEntity<RatesDto> postRates(@Valid @RequestBody RatesDto ratesDto) {
        log.debug("Posting: {}", ratesDto);
        ratesDto = rateService.addRates(ratesDto);
        return ResponseEntity.created(URI.create(PATH + "/" + ratesDto.getId())).body(ratesDto);
    }

    @GetMapping({PATH+"/{id}", PATH+"/{id}/"})
    public RatesDto getRates(@PathVariable String id) throws RaasException {
        log.debug("Getting rates for id: {}", id);
        return rateService.findRates(id)
                .orElseThrow(() -> new RaasException(ExceptionMessage.RATES_NOT_FOUND));
    }

    @DeleteMapping({PATH+"/{id}", PATH+"/{id}/"})
    public ResponseEntity<Boolean> deleteLimit(@PathVariable String id) throws RaasException {
        log.debug("Deleting limit for id: {}", id);
        if(rateService.deleteRates(id).isEmpty()) {
            throw new RaasException(ExceptionMessage.RATES_NOT_FOUND);
        }
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
