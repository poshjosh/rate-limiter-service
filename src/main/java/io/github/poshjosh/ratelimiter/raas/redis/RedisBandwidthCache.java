package io.github.poshjosh.ratelimiter.raas.redis;

import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.store.BandwidthsStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class RedisBandwidthCache implements BandwidthsStore<String> {
    private final RedisTemplate<String, Bandwidth> redisTemplate;

    public RedisBandwidthCache(RedisTemplate<String, Bandwidth> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Bandwidth get(String key) {
        final long startTime = System.currentTimeMillis();
        final Bandwidth bandwidth = redisTemplate.opsForValue().get(key);
        final long endTime = System.currentTimeMillis();
        log.trace("#get from cache in {} millis, {} = {}", (endTime - startTime), key, bandwidth);
        return bandwidth;
    }

    public void put(String key, Bandwidth bandwidth) {
        final long startTime = System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, bandwidth);
        final long endTime = System.currentTimeMillis();
        log.trace("#set to cache in {} millis, {} = {}", (endTime - startTime), key, bandwidth);
    }

    public Bandwidth remove(String key) {
        final long startTime = System.currentTimeMillis();
        final Bandwidth bandwidth = redisTemplate.opsForValue().getAndDelete(key);
        final long endTime = System.currentTimeMillis();
        log.trace("#remove from cache in {} millis, {} = {}", (endTime - startTime), key, bandwidth);
        return bandwidth;
    }
}
