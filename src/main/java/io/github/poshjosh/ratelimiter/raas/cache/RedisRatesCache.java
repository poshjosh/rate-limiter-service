package io.github.poshjosh.ratelimiter.raas.cache;

import io.github.poshjosh.ratelimiter.model.Rates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
public class RedisRatesCache {
    private static final String KEY_PREFIX = "rates::";
    private final RedisTemplate<String, Rates> redisTemplate;

    public RedisRatesCache(RedisTemplate<String, Rates> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<Rates> getAll() {
        final Set<String> keys = keys("*");
        log.info("Found {} rates in cache", keys.size());
        final List<Rates> ratesList = new ArrayList<>(keys.size());
        for (String key : keys) {
            ratesList.add(doGet(key));
        }
        return Collections.unmodifiableList(ratesList);
    }


    public Set<String> keys(String pattern) {
        pattern = keyOf(pattern);
        final Set<String> keys = redisTemplate.keys(pattern);
        return keys == null ? Collections.emptySet() : keys;
    }

    public Rates get(String key) {
        return doGet(keyOf(key));
    }

    private Rates doGet(String key) {
        final long startTime = System.currentTimeMillis();
        final Rates rates = redisTemplate.opsForValue().get(key);
        final long endTime = System.currentTimeMillis();
        log.trace("#get from cache in {} millis, {} = {}", (endTime - startTime), key, rates);
        return rates;
    }

    public void put(String key, Rates rates) {
        key = keyOf(key);
        final long startTime = System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, rates);
        final long endTime = System.currentTimeMillis();
        log.trace("#set to cache in {} millis, {} = {}", (endTime - startTime), key, rates);
    }

    public void remove(String key) {
        key = keyOf(key);
        final long startTime = System.currentTimeMillis();
        redisTemplate.delete(key);
        final long endTime = System.currentTimeMillis();
        log.trace("#remove from cache in {} millis, {}", (endTime - startTime), key);
    }

    private String keyOf(Object id) {
        return KEY_PREFIX + id;
    }
}
