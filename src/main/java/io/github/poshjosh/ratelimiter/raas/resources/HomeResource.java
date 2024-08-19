package io.github.poshjosh.ratelimiter.raas.resources;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HomeResource implements ErrorController {

    @GetMapping
    public String home() {
        log.debug("Home page");
        return "<h1>RateLimitingAsAService (RaaS)</h1>";
    }
//
//    @RequestMapping("/error")
//    public String error(HttpServletRequest request) {
//        final Object oval = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
//        final String sval = oval == null ? "Error encountered but no message" :
//                oval.toString();
//        if (log.isWarnEnabled()) {
//            log.warn(sval);
//        }
//        return sval;
//    }
}
