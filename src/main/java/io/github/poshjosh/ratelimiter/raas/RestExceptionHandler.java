package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.raas.exceptions.ExceptionMessage;
import io.github.poshjosh.ratelimiter.raas.exceptions.RaasException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.*;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        List<String> keys = ex.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .toList();
        if (keys.isEmpty()) {
            return super.handleMethodArgumentNotValid(ex, headers, status, request);
        }
        return handle(ex, headers, status, request, keys.toArray(new String[0]));
    }

    @ExceptionHandler ({ ConstraintViolationException.class} )
    protected ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        List<String> keys = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .toList();
        if (keys.isEmpty()) {
            return handle(ex, request, ExceptionMessage.BAD_REQUEST);
        }
        return handle(ex, request, keys.toArray(new String[0]));
    }

    @ExceptionHandler({ RaasException.class })
    protected ResponseEntity<Object> handleRaasException(RaasException ex, WebRequest request) {
        return handle(ex, request, ex.getExceptionMessage());
    }

    @ExceptionHandler({ AccessDeniedException.class })
    protected ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
        return handle(ex, request, ExceptionMessage.FORBIDDEN);
    }

    private ResponseEntity<Object> handle(
            Exception ex, WebRequest request, String ...keys) {
        ExceptionMessage [] msgs = Arrays.stream(keys)
                .map(ExceptionMessage::ofKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList().toArray(new ExceptionMessage[0]);
        return handle(ex, request, msgs);
    }

    private ResponseEntity<Object> handle(
            Exception ex, WebRequest request, ExceptionMessage ...msgs) {
        String [] keys = Arrays.stream(msgs).map(em -> em.key).toList().toArray(new String[0]);
        return handle(ex, new HttpHeaders(), msgs[0].status, request, keys);
    }

    private ResponseEntity<Object> handle(
            Exception ex, HttpHeaders headers, HttpStatusCode status, WebRequest request, String ...keys) {
        ProblemDetail problemDetail = new ProblemDetail() { };
        problemDetail.setDetail(buildMessageDetail(request.getLocale(), keys));
        problemDetail.setInstance(URI.create(request.getDescription(false)));
        problemDetail.setStatus(status.value());
        problemDetail.setTitle(buildMessageTitle(status));
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    private String buildMessageTitle(HttpStatusCode status) {
        HttpStatus mayBeNull = HttpStatus.resolve(status.value());
        return mayBeNull == null ? String.valueOf(status.value()) : mayBeNull.getReasonPhrase();
    }

    private String buildMessageDetail(Locale locale, String ...keys) {
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (!sb.isEmpty()) {
                sb.append("; ");
            }
            sb.append(getMessage(key, locale));
        }
        return sb.toString();
    }

    private String getMessage(String key, Locale locale) {
        MessageSource messageSource = getMessageSource();
        return messageSource == null ? "error.message" : // No MessageSource. Let the developer know
                messageSource.getMessage(key, null, key, locale);
    }
}