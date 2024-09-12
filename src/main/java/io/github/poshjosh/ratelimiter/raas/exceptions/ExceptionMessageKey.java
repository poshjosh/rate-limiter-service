package io.github.poshjosh.ratelimiter.raas.exceptions;

public interface ExceptionMessageKey {
    String REQUEST_BAD = "request.bad";
    String REQUEST_FORBIDDEN = "request.forbidden";
    String INVALID_FORMAT_PERMITS = "invalid.format.permits";
    String INVALID_FORMAT_RATES = "invalid.format.rates";
    String RATES_NOT_FOUND = "rates.not_found";
    String REQUEST_TOO_MANY = "request.too_many";
}
