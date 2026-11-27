package com.personal.marketnote.commerce.configuration;

import com.personal.marketnote.common.configuration.SchedulerLockConfig;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

// commerce-service는 shedlock.provider 미설정 → matchIfMissing=true로 RedisLockProvider 자동 활성화 (#4911 정합).
// ApplicationContextRunner로 SchedulerLockConfig만 격리 로드 + RedisConnectionFactory mock 주입.
class CommerceLockProviderIntegrationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(SchedulerLockConfig.class)
        .withBean(RedisConnectionFactory.class, () -> Mockito.mock(RedisConnectionFactory.class));

    @Test
    @DisplayName("LockProvider 빈은 정확히 1개이고 RedisLockProvider 인스턴스다")
    void lockProviderShouldBeRedisLockProvider() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LockProvider.class);
            assertThat(context.getBean(LockProvider.class)).isInstanceOf(RedisLockProvider.class);
        });
    }
}
