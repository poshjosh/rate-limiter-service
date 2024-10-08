package io.github.poshjosh.ratelimiter.raas.exceptions;

import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.Optional;

public enum ExceptionMessage implements ExceptionMessageKey {
    NOT_FOUND_RATES(ExceptionMessageKey.RATES_NOT_FOUND, HttpStatus.NOT_FOUND),
    BAD_REQUEST(ExceptionMessageKey.REQUEST_BAD),
    BAD_REQUEST_PERMITS(INVALID_FORMAT_PERMITS, HttpStatus.BAD_REQUEST),
    BAD_REQUEST_RATES(INVALID_FORMAT_RATES, HttpStatus.BAD_REQUEST),
    FORBIDDEN(ExceptionMessageKey.REQUEST_FORBIDDEN),
    TOO_MANY_REQUESTS(ExceptionMessageKey.REQUEST_TOO_MANY);

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
