package io.github.poshjosh.ratelimiter.raas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisTest extends RedisSetup {

    @Test
    void givenRedisContainerConfiguredWithDynamicProperties_thenContainerShouldBeRunning() {
        assertTrue(getRedisContainer().isRunning());
    }
}
