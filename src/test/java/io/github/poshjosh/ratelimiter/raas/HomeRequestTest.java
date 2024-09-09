package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.raas.cache.RedisInitializer;
import io.github.poshjosh.ratelimiter.raas.persistence.InitializeS3Bucket;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@InitializeS3Bucket
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HomeRequestTest implements RedisInitializer {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void homePageShouldHaveSomeContent() {
        assertThat(restTemplate
                .getForObject("http://localhost:" + port + "/", String.class)).isNotBlank();
    }
}