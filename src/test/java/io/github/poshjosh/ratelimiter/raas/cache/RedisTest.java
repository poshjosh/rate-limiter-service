package io.github.poshjosh.ratelimiter.raas.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisTest implements RedisInitializer {

    @Test
    void givenRedisContainerConfiguredWithDynamicProperties_thenContainerShouldBeRunning() {
        assertTrue(REDIS_CONTAINER.isRunning());
    }
}
