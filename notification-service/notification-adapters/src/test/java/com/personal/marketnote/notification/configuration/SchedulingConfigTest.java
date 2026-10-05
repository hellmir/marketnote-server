package com.personal.marketnote.notification.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulingConfigTest {

    @Test
    @DisplayName("ThreadPoolTaskScheduler 빈이 풀 사이즈 4로 정상 생성된다")
    void shouldCreateTaskSchedulerWithPoolSize4() {
        // given
        SchedulingConfig config = new SchedulingConfig();

        // when
        ThreadPoolTaskScheduler scheduler = config.taskScheduler();

        // then
        assertThat(scheduler).isNotNull();
        assertThat(scheduler.getPoolSize()).isEqualTo(4);
    }

    @Test
    @DisplayName("ThreadPoolTaskScheduler의 스레드 네임 접두사가 notification-scheduler-로 설정된다")
    void shouldSetThreadNamePrefix() {
        // given
        SchedulingConfig config = new SchedulingConfig();

        // when
        ThreadPoolTaskScheduler scheduler = config.taskScheduler();

        // then
        assertThat(scheduler.getThreadNamePrefix()).isEqualTo("notification-scheduler-");
    }
}
