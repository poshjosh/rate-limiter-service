package io.github.poshjosh.ratelimiter.raas.persistence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ToString
@ConfigurationProperties(prefix = "app.redis")
public class AppRedisProperties {

    @NotBlank(message = "Redis data dir must be provided")
    private String dataDir;

    @Positive(message = "Redis backup interval must be positive")
    private int backupInterval;
}