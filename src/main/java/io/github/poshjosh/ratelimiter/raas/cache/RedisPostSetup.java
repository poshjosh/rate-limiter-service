package io.github.poshjosh.ratelimiter.raas.cache;

import io.github.poshjosh.ratelimiter.bandwidths.Bandwidth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

@Slf4j
@Profile("dev")
@Configuration
public class RedisPostSetup {
    public RedisPostSetup(RedisTemplate<String, Bandwidth> redisTemplate) {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null) {
            log.info("=========================== DELETING Cache ===========================");
            keys.forEach(key -> log.info("{} = {}", key, redisTemplate.opsForValue().getAndDelete(key)));
            log.info("======================================================================");
        }
    }
}
