package io.github.poshjosh.ratelimiter.raas.cache;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public interface RedisInitializer {
    int REDIS_PORT = 6379; // Must match the port define in the main application.
    @Container
    RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis:7.0-alpine"))
                    .withExposedPorts(REDIS_PORT);

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port",
                () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());
    }
}
