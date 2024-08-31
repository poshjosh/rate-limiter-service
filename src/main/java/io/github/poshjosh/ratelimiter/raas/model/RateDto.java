package io.github.poshjosh.ratelimiter.raas.model;

import io.github.poshjosh.ratelimiter.raas.model.validation.JavaClassConstraint;
import io.github.poshjosh.ratelimiter.raas.model.validation.RateMismatchConstraint;
import lombok.*;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RateMismatchConstraint
public class RateDto {
    private static final Duration DEFAULT_DURATION = Duration.ofSeconds(1);

    @Builder.Default
    private String rate = "";

    private long permits;

    @Builder.Default
    private Duration duration = DEFAULT_DURATION;

    @Builder.Default
    private String when = "";

    @Builder.Default
    @JavaClassConstraint(message = "load.failed.factoryClass")
    private String factoryClass = "";
}
