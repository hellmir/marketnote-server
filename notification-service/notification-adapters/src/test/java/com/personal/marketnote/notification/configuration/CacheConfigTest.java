package com.personal.marketnote.notification.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    @DisplayName("RedisConnectionFactory 빈이 설정된 호스트/포트로 정상 생성된다")
    void shouldCreateRedisConnectionFactory() {
        // given
        CacheConfig config = new CacheConfig();
        ReflectionTestUtils.setField(config, "host", "localhost");
        ReflectionTestUtils.setField(config, "port", 6379);
        ReflectionTestUtils.setField(config, "password", "test-password");

        // when
        RedisConnectionFactory factory = config.redisCacheManagerFactory();

        // then
        assertThat(factory).isInstanceOf(LettuceConnectionFactory.class);
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) factory;
        assertThat(lettuceFactory.getHostName()).isEqualTo("localhost");
        assertThat(lettuceFactory.getPort()).isEqualTo(6379);
    }

    @Test
    @DisplayName("CacheManager 빈이 RedisConnectionFactory로부터 정상 생성된다")
    void shouldCreateCacheManager() {
        // given
        CacheConfig config = new CacheConfig();
        ReflectionTestUtils.setField(config, "host", "localhost");
        ReflectionTestUtils.setField(config, "port", 6379);
        ReflectionTestUtils.setField(config, "password", "test-password");
        RedisConnectionFactory factory = config.redisCacheManagerFactory();

        // when
        CacheManager cacheManager = config.redisCacheManager(factory);

        // then
        assertThat(cacheManager).isInstanceOf(RedisCacheManager.class);
    }
}
