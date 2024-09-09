package io.github.poshjosh.ratelimiter.raas.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.Optional;

public enum ExceptionMessage implements ExceptionMessageKey {
    RATES_NOT_FOUND(ExceptionMessageKey.RATES_NOT_FOUND, HttpStatus.NOT_FOUND),
    BAD_REQUEST(ExceptionMessageKey.BAD_REQUEST),
    BAD_REQUEST_PERMITS(INVALID_FORMAT_PERMITS, HttpStatus.BAD_REQUEST),
    BAD_REQUEST_RATES(INVALID_FORMAT_RATES, HttpStatus.BAD_REQUEST),
    FORBIDDEN(ExceptionMessageKey.FORBIDDEN),
    TOO_MANY_REQUESTS(ExceptionMessageKey.TOO_MANY_REQUESTS);

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

    public static Optional<ExceptionMessage> ofKey(String key) {
        for (ExceptionMessage message : values()) {
            if (message.key.equals(key)) {
                return Optional.of(message);
            }
        }
        return Optional.empty();
    }
}
