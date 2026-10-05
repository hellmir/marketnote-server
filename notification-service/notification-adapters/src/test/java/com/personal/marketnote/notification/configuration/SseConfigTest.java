package com.personal.marketnote.notification.configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;

import static org.assertj.core.api.Assertions.assertThatCode;

class SseConfigTest {

    @Test
    @DisplayName("AsyncSupportConfigurer에 기본 타임아웃이 정상 설정된다")
    void shouldConfigureAsyncSupportWithDefaultTimeout() {
        // given
        SseConfig config = new SseConfig();
        ReflectionTestUtils.setField(config, "emitterTimeoutMs", 1800000L);
        AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();

        // when & then
        assertThatCode(() -> config.configureAsyncSupport(configurer))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("커스텀 타임아웃 값으로 AsyncSupportConfigurer가 설정된다")
    void shouldConfigureAsyncSupportWithCustomTimeout() {
        // given
        SseConfig config = new SseConfig();
        ReflectionTestUtils.setField(config, "emitterTimeoutMs", 3600000L);
        AsyncSupportConfigurer configurer = new AsyncSupportConfigurer();

        // when & then
        assertThatCode(() -> config.configureAsyncSupport(configurer))
                .doesNotThrowAnyException();
    }
}
