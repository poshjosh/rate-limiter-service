package io.github.poshjosh.ratelimiter.raas.exceptions;

public interface ExceptionMessageKey {
    String BAD_REQUEST = "request.bad";
    String FORBIDDEN = "request.forbidden";
    String INVALID_FORMAT_PERMITS = "invalid.format.permits";
    String INVALID_FORMAT_RATES = "invalid.format.rates";
    String RATES_NOT_FOUND = "rates.not_found";
    String TOO_MANY_REQUESTS = "request.too_many";
}
