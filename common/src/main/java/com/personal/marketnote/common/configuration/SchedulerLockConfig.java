package com.personal.marketnote.common.configuration;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

// #4947: RedisLockProvider 3-arg 생성자(env, keyPrefix)로 환경/서비스별 키 분리.
// 키 형식: "<keyPrefix>:<environment>:<lockName>" → "shop-job-lock:prod:commerce-service-settlement"
// - environment 분리: qa/stage/prod 키 충돌 방지 + 환경 간 락 탈취 차단
// - keyPrefix 변경: 기본값 "job-lock" → "shop-job-lock"으로 enumeration 표면 축소(KEYS job-lock:* 패턴 무력화)
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class SchedulerLockConfig {

    private static final String LOCK_KEY_PREFIX = "shop-job-lock";

    @Bean
    @ConditionalOnProperty(prefix = "shedlock", name = "provider", havingValue = "redis", matchIfMissing = true)
    @ConditionalOnBean(RedisConnectionFactory.class)
    public LockProvider lockProvider(
        RedisConnectionFactory redisConnectionFactory,
        @Value("${spring.profiles.active:default}") String activeProfile
    ) {
        return new RedisLockProvider(redisConnectionFactory, activeProfile, LOCK_KEY_PREFIX);
    }
}
