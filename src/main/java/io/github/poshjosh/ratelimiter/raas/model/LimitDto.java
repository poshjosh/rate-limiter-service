package io.github.poshjosh.ratelimiter.raas.model;

import jakarta.validation.Valid;
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
    private int permits = 1;
    @Builder.Default
    private boolean async = false;
    @Valid private RatesDto limit;
    @Valid private HttpRequestDto request;
}
