package io.github.poshjosh.ratelimiter.raas.cache;

import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import io.github.poshjosh.ratelimiter.model.Rates;
import io.github.poshjosh.ratelimiter.raas.persistence.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;

@Slf4j
@Configuration
public class RedisConfiguration {
    public RedisConfiguration(BackupService backupService) throws IOException {
        // This must be done before Redis is initialized. So we do it here.
        backupService.restoreRedisDataFromBackup();
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(@Value("${spring.data.redis.host}") String host,
                                                         @Value("${spring.data.redis.port}") int port) {
        log.info("Will connect to Redis at: {}:{}", host, port);
        return new JedisConnectionFactory(
                new RedisStandaloneConfiguration(host, port)
        );
    }

    @Bean("redisBandwidthTemplate")
    public RedisTemplate<String, Bandwidth> redisBandwidthTemplate(RedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, Bandwidth> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
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
    public RedisTemplate<String, Rates> redisRatesTemplate(RedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, Rates> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
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
