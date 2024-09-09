package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.raas.cache.RedisInitializer;
import io.github.poshjosh.ratelimiter.raas.model.RateDto;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import io.github.poshjosh.ratelimiter.raas.persistence.InitializeS3Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@InitializeS3Bucket
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HappyPathTest implements RedisInitializer {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate.getRestTemplate().getInterceptors()
                .add(new HttpHeaderInterceptor("Content-Type", "application/json"));
    }

    @Test
    void testHappyPath() {
        final String url = "http://localhost:" + port ;

        final String rateId = this.getClass().getSimpleName();
        final RateDto rate = RateDto.builder().rate("1/s").build();
        final RatesDto rates = RatesDto.builder().id(rateId).rates(List.of(rate)).build();

        restTemplate.postForObject(url + "/rates", rates, RatesDto.class);

        RatesDto result = restTemplate.getForObject(url + "/rates/" + rateId, RatesDto.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(rateId);

        assertThat(restTemplate.exchange(
                        url + "/permits/available?rateId=" + rateId, HttpMethod.PUT, null, Boolean.class)
                .getBody()).isTrue();

        assertThat(restTemplate.exchange(
                url + "/permits/acquire?rateId=" + rateId, HttpMethod.PUT, null, Boolean.class)
                .getBody()).isTrue();

        ResponseEntity<ProblemDetail> response = restTemplate.exchange(
                url + "/permits/acquire?rateId=" + rateId, HttpMethod.PUT, null, ProblemDetail.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        restTemplate.delete(url + "/rates/" + rateId, Boolean.class);

        ProblemDetail problem = restTemplate.getForObject(url + "/rates/" + rateId, ProblemDetail.class);
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(404);
    }
}