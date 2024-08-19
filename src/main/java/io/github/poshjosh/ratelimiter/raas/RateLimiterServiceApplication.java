package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.raas.util.EnvLogger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class RateLimiterServiceApplication {
    public static void main(String[] args) {
        Environment env = SpringApplication
                .run(RateLimiterServiceApplication.class, args).getEnvironment();
        EnvLogger.log(env);
    }
}
