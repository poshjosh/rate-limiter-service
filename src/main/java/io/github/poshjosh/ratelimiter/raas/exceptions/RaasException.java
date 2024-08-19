package io.github.poshjosh.ratelimiter.raas.exceptions;

import lombok.Getter;

import java.util.Objects;

@Getter
public class RaasException extends Exception {

    private final ExceptionMessage exceptionMessage;

    public RaasException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.key);
        this.exceptionMessage = Objects.requireNonNull(exceptionMessage);
    }

    public RaasException(ExceptionMessage exceptionMessage, Throwable cause) {
        super(exceptionMessage.key, cause);
        this.exceptionMessage = exceptionMessage;
    }
}
