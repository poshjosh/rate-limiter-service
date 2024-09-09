package io.github.poshjosh.ratelimiter.raas.model;

import io.github.poshjosh.ratelimiter.raas.model.validation.RatesOperatorConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RatesOperatorConstraint
public class RatesDto {

    private String parentId;

    @NotBlank(message = "required.id")
    private String id;

    @Builder.Default
    private Operator operator = Operator.NONE;

    @NotEmpty(message = "required.rates")
    private List<RateDto> rates;

    private String when;
}
