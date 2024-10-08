package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.raas.cache.RedisInitializer;
import io.github.poshjosh.ratelimiter.raas.persistence.InitializeS3Bucket;
import io.github.poshjosh.ratelimiter.raas.resources.HomeResource;
import io.github.poshjosh.ratelimiter.raas.resources.RateResource;
import io.github.poshjosh.ratelimiter.raas.resources.PermitResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@InitializeS3Bucket
class SmokeTest implements RedisInitializer {

    @Autowired HomeResource homeResource;
    @Autowired RateResource rateResource;
    @Autowired PermitResource permitResource;

    @Test void contextShouldLoadHomeResource() {
        assertThat(homeResource).isNotNull();
    }

    @Test void contextShouldLoadLimitResource() {
        assertThat(rateResource).isNotNull();
    }

    @Test void contextShouldLoadPermitResource() {
        assertThat(permitResource).isNotNull();
    }
}
