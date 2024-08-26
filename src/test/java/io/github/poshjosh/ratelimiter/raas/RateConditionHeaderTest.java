package io.github.poshjosh.ratelimiter.raas;

import io.github.poshjosh.ratelimiter.raas.model.HttpRequestDto;
import io.github.poshjosh.ratelimiter.raas.model.RateDto;
import io.github.poshjosh.ratelimiter.raas.model.RatesDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.devtools.remote.client.HttpHeaderInterceptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class RateConditionHeaderTest extends RedisSetup {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final String rateId = this.getClass().getSimpleName();

    @BeforeEach
    void setUp() {
        restTemplate.getRestTemplate().getInterceptors()
                .add(new HttpHeaderInterceptor("Content-Type", "application/json"));
    }

    @AfterEach
    void tearDown() {
        restTemplate.delete(url("/rates/" + rateId), String.class);
    }

    @ParameterizedTest
    @CsvSource({
            "'', '', '', 429",
            "'web.request.header[X-RATE-LIMIT] = true', '', '', 200",
// TODO fix this test case, which fails when run with others, but not when run alone.
//            "'', 'X-RATE-LIMIT', 'true', 429",
            "'web.request.header[X-RATE-LIMIT] = true', 'X-RATE-LIMIT', 'true', 429"
    })
    void testRateConditionHeader(
            String rateCondition, String headerName, String headerValue, int expectedStatus) {

        // Post rate
        final RateDto rate = RateDto.builder().rate("1/h").when(rateCondition).build();
        final RatesDto rates = RatesDto.builder().id(rateId).rates(List.of(rate)).build();
        restTemplate.postForObject(url("/rates"), rates, RatesDto.class);

        // Acquire one permit
        final String url = url("/permits/acquire?rateId=" + rateId);
        assertThat(restTemplate.exchange(
                url, HttpMethod.PUT, request(headerName, headerValue), Boolean.class).getBody())
                .isTrue();

        // Acquire one more permit
        assertThat(restTemplate.exchange(
                url, HttpMethod.PUT, request(headerName, headerValue), String.class).getStatusCode().value())
                .isEqualTo(expectedStatus);
    }

    private HttpEntity<HttpRequestDto> request(String headerName, String headerValue) {
        return new HttpEntity<>(requestBody(headerName, headerValue));
    }

    private HttpRequestDto requestBody(String headerName, String headerValue) {
        Map<String, List<String>> headers = headerName == null || headerName.isBlank() ?
                Collections.emptyMap() : Map.of(headerName, List.of(headerValue));
        return HttpRequestDto.builder()
                .requestUri("http://localhost/8081/basket")
                .servletPath("/checkout")
                .method("GET")
                .contextPath("")
                .headers(headers).build();
    }

    private String url(String suffix) {
        return "http://localhost:" + port + Objects.requireNonNull(suffix);
    }
}