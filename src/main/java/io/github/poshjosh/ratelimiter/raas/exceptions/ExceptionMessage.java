package io.github.poshjosh.ratelimiter.raas.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public enum ExceptionMessage {
    RATES_NOT_FOUND("msgs.ex.rates.not_found", HttpStatus.NOT_FOUND),
    BAD_REQUEST("msgs.ex.request.bad", HttpStatus.BAD_REQUEST),
    FORBIDDEN("msgs.ex.request.forbidden", HttpStatus.FORBIDDEN),
    CONFLICT("msgs.ex.request.conflict", HttpStatus.CONFLICT);

    public final String key;
    public final HttpStatus status;

    ExceptionMessage(String key, HttpStatus status) {
        this.key = Objects.requireNonNull(key);
        this.status = Objects.requireNonNull(status);
    }
}
