package io.github.poshjosh.ratelimiter.raas.redis;

import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.model.Rates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfiguration {
    private final RedisConnectionFactory connectionFactory;

    public RedisConfiguration(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean("redisBandwidthTemplate")
    public RedisTemplate<String, Bandwidth> redisBandwidthTemplate() {
        final RedisTemplate<String, Bandwidth> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }

    @Bean
    public RedisBandwidthCache redisBandwidthStore(
            @Autowired @Qualifier("redisBandwidthTemplate")
            RedisTemplate<String, Bandwidth> redisBandwidthTemplate) {
        return new RedisBandwidthCache(redisBandwidthTemplate);
    }

    @Bean("redisRatesTemplate")
    public RedisTemplate<String, Rates> redisRatesTemplate() {
        final RedisTemplate<String, Rates> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(this.connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        return redisTemplate;
    }

    @Bean
    public RedisRatesCache redisRatesCache(
            @Autowired @Qualifier("redisRatesTemplate")
            RedisTemplate<String, Rates> redisRatesTemplate) {
        return new RedisRatesCache(redisRatesTemplate);
    }
}
