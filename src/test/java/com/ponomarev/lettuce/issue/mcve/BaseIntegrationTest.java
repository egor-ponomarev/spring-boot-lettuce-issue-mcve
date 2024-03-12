package com.ponomarev.lettuce.issue.mcve;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = RedisAutoConfiguration.class)
public class BaseIntegrationTest {
    private static final String REDIS_IMAGE = "6.2-alpine";

    @Container
    public static final RedisContainer container = new RedisContainer(REDIS_IMAGE)
            .withExposedPorts(RedisContainer.REDIS_PORT);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", container::getHost);
        registry.add("spring.data.redis.port", () -> container.getMappedPort(RedisContainer.REDIS_PORT));
    }
}
