package io.github.poshjosh.ratelimiter.raas.model;

import io.github.poshjosh.ratelimiter.annotations.VisibleForTesting;
import io.github.poshjosh.ratelimiter.model.Rate;
import io.github.poshjosh.ratelimiter.model.Rates;
import io.github.poshjosh.ratelimiter.raas.exceptions.ExceptionMessage;
import io.github.poshjosh.ratelimiter.raas.exceptions.RaasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class RateMapper {

    public Rates toEntity(RatesDto dto) {
        final Operator optr = dto.getOperator();
        final io.github.poshjosh.ratelimiter.model.Operator operator =
                optr == null ? null :
                        io.github.poshjosh.ratelimiter.model.Operator.valueOf(optr.name());
        final List<Rate> rates = dto.getRates()
                .stream()
                .filter(Objects::nonNull)
                .map(this::toEntity)
                .toList();
        return new Rates().id(dto.getId())
                .parentId(dto.getParentId())
                .operator(operator)
                .condition(dto.getWhen())
                .rates(rates);
    }

    public Rate toEntity(RateDto dto) {
        return new Rate().permits(dto.getPermits())
                .duration(dto.getDuration())
                .rate(dto.getRate())
                .condition(dto.getWhen())
                .factoryClass(dto.getFactoryClass());
    }

    public RatesDto toDto(Rates entity) {
        final List<RateDto> rates = new ArrayList<>();
        entity.getRates()
                .stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .collect(Collectors.toCollection(() -> rates));
        return RatesDto.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .operator(entity.getOperator() == null ? null :
                        Operator.valueOf(entity.getOperator().name()))
                .rates(rates)
                .when(entity.getCondition())
                .build();
    }

    public RateDto toDto(Rate entity) {
        return RateDto.builder()
                .rate(entity.getRate())
                .permits(entity.getPermits())
                .duration(entity.getDuration())
                .when(entity.getCondition())
                .factoryClass(entity.getFactoryClass())
                .build();
    }

    /**
     * Converts a tree of rates to a list of {@link RatesDto} objects.
     * <p>Example input:</p>
     * <code>
     *     {
     *     "grand-parent-0": {
     *         "rates": [],
     *         "operator": "OR",
     *         "parent-0": {
     *             "rates": [{"rate": "5/s"}, {"rate": "1/s", "when": "web.request.user.role = GUEST"}]
     *         },
     *         "parent-1": {
     *             "rates": [],
     *             "parent-1-child-0": {
     *                 "rates": [{"rate": "30/m"}],
     *                 "when": "jvm.memory.available < 1gb"
     *             }
     *         }
     *     }
     * }
     * </code>
     *
     * @param tree The tree of rates to convert to a list of {@link RatesDto} objects.
     * @return The list of {@link RatesDto} objects.
     * @throws RaasException if the input Map contains badly values of the wrong format.
     */
    public List<RatesDto> toDtos(Map<String, Object> tree) throws RaasException {
        final List<RatesDto> result = new ArrayList<>();
        collectTree(stringValue(tree, "id"), tree, result::add);
        return Collections.unmodifiableList(result);
    }

    private void collectTree(String id, Map<String, Object> tree, Consumer<RatesDto> consumer)
            throws RaasException {

        final Map<String, Object> target = collectNonNullLeafValues(tree);
        if (id != null) {
            target.put("id", id); // Takes precedence over any specified in the map
        }
        consumer.accept(toRatesDto(target));

        for (Map.Entry<String, Object> entry : tree.entrySet()) {
            final Object val = entry.getValue();
            if (val instanceof Map) {
                Map<String, Object> child = (Map<String, Object>)val;
                if (id != null) {
                    child.put("parentId", id); // Takes precedence over any specified in map
                }
                collectTree(entry.getKey(), child, consumer);
            }
        }
    }

    @VisibleForTesting
    RatesDto toRatesDto(Map<String, Object> map) throws RaasException {
        final String operatorStr = stringValue(map, "operator");
        final Object ratesObj = map.get("rates");
        if (ratesObj != null && !(ratesObj instanceof List)) {
            throw new RaasException(ExceptionMessage.BAD_REQUEST_RATES);
        }
        final List<Map<String, Object>> rates = (List<Map<String, Object>>)ratesObj;
        final List<RateDto> rateDtos;
        if (rates == null) {
            rateDtos = null;
        } else {
            rateDtos = new ArrayList<>(rates.size());
            for(Map<String, Object> rate : rates) {
                rateDtos.add(toRateDto(rate));
            }
        }
        return RatesDto.builder()
                .operator(operatorStr == null || operatorStr.isBlank() ? null : Operator.valueOf(operatorStr))
                .parentId(stringValue(map, "parentId"))
                .id(stringValue(map, "id"))
                .when(stringValue(map, "when"))
                .rates(rateDtos)
                .build();
    }

    @VisibleForTesting
    Map<String, Object> toMap(RatesDto ratesDto) {
        final String operator = ratesDto.getOperator() == null
                ? null : ratesDto.getOperator().name();
        final List<Map<String, Object>> rates = ratesDto.getRates() == null ? null :
                ratesDto.getRates().stream().map(this::toMap).toList();
        Map<String, Object> map = new HashMap<>();
        addIfNonNull(map, "parentId", ratesDto.getParentId());
        addIfNonNull(map, "id", ratesDto.getId());
        addIfNonNull(map, "operator", operator);
        addIfNonNull(map, "rates", rates);
        addIfNonNull(map, "when", ratesDto.getWhen());
        return Collections.unmodifiableMap(map);
    }

    @VisibleForTesting
    RateDto toRateDto(Map<String, Object> map) throws RaasException {
        final String durationText = stringValue(map, "duration");
        return RateDto.builder()
                .rate(stringValue(map, "rate"))
                .permits(permits(map))
                .duration(durationText == null || durationText.isBlank() ? null : Duration.parse(durationText))
                .when(stringValue(map, "when"))
                .factoryClass(stringValue(map, "factoryClass"))
                .build();
    }

    @VisibleForTesting
    Map<String, Object> toMap(RateDto rate) {
        Map<String, Object> map = new HashMap<>();
        addIfNonNull(map, "rate", rate.getRate());
        addIfNonNull(map, "permits", rate.getPermits());
        addIfNonNull(map, "duration", rate.getDuration());
        addIfNonNull(map, "when", rate.getWhen());
        addIfNonNull(map, "factoryClass", rate.getFactoryClass());
        return Collections.unmodifiableMap(map);
    }

    private void addIfNonNull(Map<String, Object> map, String key, Object val) {
        if (val != null) {
            map.put(key, val);
        }
    }

    private static final Predicate<Object> nonNullLeaf =
            value -> !(value instanceof Map) && value != null;
    private static Map<String, Object> collectNonNullLeafValues(Map<String, Object> tree) {
        final Map<String, Object> result = new HashMap<>(tree.size());
        tree.entrySet().stream()
                .filter(entry -> nonNullLeaf.test(entry.getValue()))
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    private String stringValue(Map<String, Object> map, String key) {
        final Object val = map.get(key);
        return val == null ? null : val.toString();
    }
    private long permits(Map<String, Object> map) throws RaasException {
        final Object val = map.get("permits");
        try {
            return val == null ? 0 : Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            throw new RaasException(ExceptionMessage.BAD_REQUEST_PERMITS);
        }
    }
}
