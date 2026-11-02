package com.personal.marketnote.notification.adapter.in.web.sse.controller;

import com.personal.marketnote.notification.adapter.in.web.sse.registry.SseConnectionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SseNotificationControllerTest {

    private OAuth2AuthenticatedPrincipal buildPrincipal(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    @Test
    @DisplayName("SSE 스트림을 구독하면 SseEmitter를 반환한다")
    void shouldReturnSseEmitterOnSubscribe() {
        // given
        SseConnectionRegistry registry = mock(SseConnectionRegistry.class);
        SseNotificationController controller = new SseNotificationController(registry, 1800000L);
        OAuth2AuthenticatedPrincipal principal = buildPrincipal(100L);
        SseEmitter expectedEmitter = new SseEmitter(1800000L);
        when(registry.register(100L, 1800000L)).thenReturn(expectedEmitter);

        // when
        SseEmitter result = controller.subscribe(principal);

        // then
        assertThat(result).isEqualTo(expectedEmitter);
    }

    @Test
    @DisplayName("커스텀 타임아웃으로 SseEmitter를 생성한다")
    void shouldCreateEmitterWithCustomTimeout() {
        // given
        SseConnectionRegistry registry = mock(SseConnectionRegistry.class);
        SseNotificationController controller = new SseNotificationController(registry, 3600000L);
        OAuth2AuthenticatedPrincipal principal = buildPrincipal(200L);
        SseEmitter expectedEmitter = new SseEmitter(3600000L);
        when(registry.register(200L, 3600000L)).thenReturn(expectedEmitter);

        // when
        SseEmitter result = controller.subscribe(principal);

        // then
        assertThat(result).isEqualTo(expectedEmitter);
    }
}
