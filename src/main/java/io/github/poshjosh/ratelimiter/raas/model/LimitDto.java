package io.github.poshjosh.ratelimiter.raas.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitDto {
    @Builder.Default
    @Positive
    private int permits = 1;
    @Builder.Default
    private boolean async = false;
    @Valid private RatesDto limit;
    @Valid private HttpRequestDto request;
}
