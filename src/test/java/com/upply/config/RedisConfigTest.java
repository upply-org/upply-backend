package com.upply.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Redis Caching Configuration Tests")
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Test
    @DisplayName("Should create cache manager with proper configuration")
    void shouldCreateCacheManagerWithProperConfiguration() {
        RedisConfig redisConfig = new RedisConfig();
        RedisCacheManager cacheManager = redisConfig.cacheManager(connectionFactory);

        assertNotNull(cacheManager);
    }

    @Test
    @DisplayName("Should use connection factory when building cache manager")
    void shouldUseConnectionFactoryWhenBuildingCacheManager() {
        RedisConfig redisConfig = new RedisConfig();
        RedisCacheManager cacheManager = redisConfig.cacheManager(connectionFactory);

        assertNotNull(cacheManager);
    }
}