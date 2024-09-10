package io.github.poshjosh.ratelimiter.raas.resources;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeResource implements ErrorController {
    private final MessageSource messageSource;

    @GetMapping
    public String home() {
        log.debug("Home page");
        return "<h1>RateLimitingAsAService (RaaS)</h1>";
    }

    @RequestMapping("/error")
    public String error(HttpServletRequest request) {
        log.debug("Error page");
        final Locale locale = request.getLocale();
        final String title = translate("error.title", locale);
        final Object oval = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        final String message = oval != null ? oval.toString() : translate("error.message", locale);
        log.warn(message);
        return "<h1>" + title + "</h1><h2>" + message + "</h2>";
    }

    private String translate(String key, Locale locale) {
        return messageSource.getMessage(key, null, key, locale);
    }
}
