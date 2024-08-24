package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.raas.exceptions.ExceptionMessage;
import io.github.poshjosh.ratelimiter.raas.exceptions.RaasException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ RaasException.class })
    protected ResponseEntity<Object> handleBusinessException(RaasException ex, WebRequest request) {
        return handle(ex, request, ex.getExceptionMessage());
    }

    @ExceptionHandler({ AccessDeniedException.class })
    protected ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
        return handle(ex, request, ExceptionMessage.FORBIDDEN);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        return handle(ex, request, ExceptionMessage.CONFLICT);
    }

    private ResponseEntity<Object> handle(Exception ex, WebRequest request, ExceptionMessage msg) {
        ProblemDetail problemDetail = new ProblemDetail() { };
        problemDetail.setDetail(msg.key);
        problemDetail.setInstance(URI.create(request.getDescription(false)));
        problemDetail.setStatus(msg.status.value());
        problemDetail.setTitle(msg.key);
        return handleExceptionInternal(ex, problemDetail, new HttpHeaders(), msg.status, request);
    }
}