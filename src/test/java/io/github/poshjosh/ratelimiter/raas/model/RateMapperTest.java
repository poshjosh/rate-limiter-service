package io.github.poshjosh.ratelimiter.raas.model;

import io.github.poshjosh.ratelimiter.model.Rate;
import io.github.poshjosh.ratelimiter.model.Rates;
import io.github.poshjosh.ratelimiter.raas.exceptions.RaasException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RateMapperTest {

    private final RateMapper rateMapper = new RateMapper();

    @ParameterizedTest
    @CsvSource({
            "9234156, SECONDS",
            "10, MINUTES",
            "3, HOURS"})
    void toEntity_shouldReturnValidEntity(final int permits, final TimeUnit timeUnit) {
        final String rate = Rate.rate(permits, timeUnit);
        final RatesDto ratesDto = RatesDto.builder()
                .id("id")
                .parentId("parentId")
                .rates(Collections.singletonList(RateDto.builder().rate(rate).build()))
                .when("condition")
                .operator(Operator.NONE)
                .build();
        final Rates rates = rateMapper.toEntity(ratesDto);
        assertThat(rates.getId()).isEqualTo("id");
        assertThat(rates.getParentId()).isEqualTo("parentId");
        assertThat(rates.getRates()).hasSize(1);
        assertThat(rates.getRates().get(0).getRate()).isEqualTo(rate);
        assertThat(rates.getCondition()).isEqualTo("condition");
        assertThat(rates.getOperator()).isEqualTo(
                io.github.poshjosh.ratelimiter.model.Operator.NONE);
    }

    @ParameterizedTest
    @CsvSource({
            "1, PT1S",
            "2, PT1M",
            "3, PT1H",
            "4, PT24H",
            "20, PT2S",
            "5000, PT5H",
    })
    void toDto_shouldReturnValidDto(int permits, Duration duration) {
        Rate entity = Rate.of(permits, duration);
        RateDto dto = rateMapper.toDto(entity);
        assertThat(dto.getRate()).isEqualTo(entity.getRate());
        assertThat(dto.getPermits()).isEqualTo(entity.getPermits());
        assertThat(dto.getDuration()).isEqualTo(entity.getDuration());
        assertThat(dto.getWhen()).isEqualTo(entity.getCondition());
        assertThat(dto.getFactoryClass()).isEqualTo(entity.getFactoryClass());
    }

    @ParameterizedTest
    @CsvSource({
            "99/h, 0, PT0S, condition, ''",
            "'', 99, PT3M, condition, ''"
    })
    void toRateDto_shouldReturnValidDto(
            String rate, long permits, Duration duration, String when, String factoryClass)
            throws RaasException {
        Map<String, Object> rateMap = rateMapOf(rate, permits, duration, when, factoryClass);
        RateDto dto = rateMapper.toRateDto(rateMap);
        assertThat(dto.getRate()).isEqualTo(rate);
        assertThat(dto.getPermits()).isEqualTo(permits);
        assertThat(dto.getDuration()).isEqualTo(duration);
        assertThat(dto.getWhen()).isEqualTo(when);
        assertThat(dto.getFactoryClass()).isEqualTo(factoryClass);
    }

    @ParameterizedTest
    @CsvSource({
            "99/h, 99, PT1H, condition, ''",
            "'', 0, PT0S, condition, ''",
            "99/h, 0, PT0S, condition, 'fake-class'"
    })
    void toRateDto_shouldSucceed_givenInvalidMap(
            String rate, long permits, Duration duration, String when, String factoryClass)
            throws RaasException {
        Map<String, Object> rateMap = rateMapOf(rate, permits, duration, when, factoryClass);
        rateMapper.toRateDto(rateMap);
    }


    @ParameterizedTest
    @CsvSource({
            "A, 1, AND, global-condition, 99/h, 0, PT0S, condition, ''",
            "B, 2, OR, '', '', 99, PT3M, condition, ''"
    })
    void toRatesDto_shouldReturnValidDto(
            String parentId, String id, Operator operator, String globalWhen,
            String rate, long permits, Duration duration, String when, String factoryClass)
            throws RaasException {
        Map<String, Object> ratesMap = ratesMapOf(
                parentId, id, operator, globalWhen,
                rate, permits, duration, when, factoryClass);
        RatesDto dto = rateMapper.toRatesDto(ratesMap);
        assertThat(dto.getParentId()).isEqualTo(parentId);
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getOperator()).isEqualTo(operator);
        assertThat(dto.getWhen()).isEqualTo(globalWhen);
        assertThat(dto.getRates()).hasSize(2);
    }

    @ParameterizedTest
    @CsvSource({
            "A, 1, AND, global-condition, '', 0, PT0S, condition, ''",
            "A, 1, AND, global-condition, '99/s', 99, PT1S, condition, ''",
            "A, 1, NONE, global-condition, '99/s', 0, PT0S, condition, ''"
    })
    void toRatesDto_shouldFail_givenInvalidMap(
            String parentId, String id, Operator operator, String globalWhen,
            String rate, long permits, Duration duration, String when, String factoryClass)
            throws RaasException {
        Map<String, Object> ratesMap = ratesMapOf(
                parentId, id, operator, globalWhen,
                rate, permits, duration, when, factoryClass);
        rateMapper.toRatesDto(ratesMap);
    }

    Map<String, Object> ratesMapOf(
            String parentId, String id, Operator operator, String globalWhen,
            String rate, long permits, Duration duration, String when, String factoryClass) {
        Map<String, Object> rateMap = rateMapOf(rate, permits, duration, when, factoryClass);
        RatesDto dto = RatesDto.builder()
                .parentId(parentId).id(id).operator(operator).when(globalWhen).build();
        Map<String, Object> result = new HashMap<>(rateMapper.toMap(dto));
        result.put("rates", Arrays.asList(rateMap, rateMap));
        return result;
    }

    private Map<String, Object> rateMapOf(
            String rate, long permits, Duration duration, String when, String factoryClass) {
        RateDto dto = RateDto.builder()
                .rate(rate).permits(permits).duration(duration)
                .when(when).factoryClass(factoryClass).build();
        return rateMapper.toMap(dto);
    }

    @Test
    void toDtos_shouldReturnValidDtos() throws RaasException {
        Map<String, Object> validTree = givenValidTree();
        List<RatesDto> result = rateMapper.toDtos(validTree);
        final String [] ids = {"parent", "child", "grandChild1", "grandChild2", "greatGrandChild"};
        assertThat(result).hasSize(ids.length);
        for (int i = 0; i < result.size(); i++) {
            RatesDto dto = result.get(i);
            assertThat(dto.getId()).isEqualTo(ids[i]);
            assertThat(dto.getRates()).hasSize(1);
        }
    }

    private Map<String, Object> givenValidTree() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> child = new LinkedHashMap<>();
        Map<String, Object> grandChild1 = new LinkedHashMap<>();
        Map<String, Object> grandChild2 = new LinkedHashMap<>();
        Map<String, Object> greatGrandChild = new LinkedHashMap<>();

        root.put("id", "parent");
        root.put("rates", Collections.singletonList(Map.of("rate", "1/s")));
        root.put("child", child);

        child.put("id", "child.id"); // Will be overridden
        child.put("rates", Collections.singletonList(Map.of("rate", "3/s")));
        child.put("grandChild1", grandChild1);
        child.put("grandChild2", grandChild2);

        grandChild1.put("id", "grandChild1.id"); // Will be overridden
        grandChild1.put("rates", Collections.singletonList(Map.of("rate", "1/s")));

        grandChild2.put("id", "grandChild2.id"); // Will be overridden
        grandChild2.put("rates", Collections.singletonList(Map.of("rate", "2/s")));
        grandChild2.put("greatGrandChild", greatGrandChild);

        greatGrandChild.put("id", "greatGrandChild.id"); // Will be overridden
        greatGrandChild.put("rates", Collections.singletonList(Map.of("rate", "1/s")));
        return root;
    }
}
