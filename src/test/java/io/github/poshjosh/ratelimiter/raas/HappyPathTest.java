package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.raas.model.RateDto;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ProblemDetail;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HappyPathTest extends RedisSetup {

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
    void test() {
        final String url = "http://localhost:" + port ;

        final String rateId = this.getClass().getSimpleName();
        final RateDto rate = RateDto.builder().rate("1/s").build();
        final RatesDto rates = RatesDto.builder().id(rateId).rates(List.of(rate)).build();

        restTemplate.postForObject(url + "/rates", rates, RatesDto.class);

        RatesDto result = restTemplate.getForObject(url + "/rates/" + rateId, RatesDto.class);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(rateId);

        assertThat(restTemplate.postForObject(
                url + "/permits/available?rateId=" + rateId, null, Boolean.class)).isTrue();

        assertThat(restTemplate.postForObject(
                url + "/permits/acquire?rateId=" + rateId, null, Boolean.class)).isTrue();

        assertThat(restTemplate.postForObject(
                url + "/permits/acquire?rateId=" + rateId, null, Boolean.class)).isFalse();

        restTemplate.delete(url + "/rates/" + rateId, String.class);

        ProblemDetail problem = restTemplate.getForObject(url + "/rates/" + rateId, ProblemDetail.class);
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(404);
    }
}