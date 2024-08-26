package io.github.poshjosh.ratelimiter.raas.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Objects;

public enum ExceptionMessage {
    RATES_NOT_FOUND("msgs.ex.rates.not_found", HttpStatus.NOT_FOUND),
    BAD_REQUEST("msgs.ex.request.bad"),
    FORBIDDEN("msgs.ex.request.forbidden"),
    CONFLICT("msgs.ex.request.conflict"),
    TOO_MANY_REQUESTS("msgs.ex.request.too_many");

    public final String key;
    public final HttpStatus status;

    ExceptionMessage(String key) {
        this.key = Objects.requireNonNull(key);
        this.status = HttpStatus.valueOf(name());
    }

    ExceptionMessage(String key, HttpStatus status) {
        this.key = Objects.requireNonNull(key);
        this.status = Objects.requireNonNull(status);
    }
}
