package com.personal.marketnote.notification.adapter.in.web.sse.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SseConnectionRegistryTest {

    private SseConnectionRegistry sseConnectionRegistry;

    @BeforeEach
    void setUp() {
        sseConnectionRegistry = new SseConnectionRegistry(5000);
    }

    @Test
    @DisplayName("SseEmitter 생성 후 Registry에 등록한다")
    void shouldRegisterSseEmitterForUser() {
        // given
        Long userId = 1L;
        long timeoutMs = 30000L;

        // when
        SseEmitter emitter = sseConnectionRegistry.register(userId, timeoutMs);

        // then
        assertThat(emitter).isNotNull();
        assertThat(sseConnectionRegistry.getConnectionCount()).isEqualTo(1);
        assertThat(sseConnectionRegistry.getConnectedUserIds()).contains(userId);
    }

    @Test
    @DisplayName("remove 호출 시 Registry에서 제거한다")
    void shouldRemoveFromRegistryOnRemove() {
        // given
        Long userId = 1L;
        sseConnectionRegistry.register(userId, 30000L);
        assertThat(sseConnectionRegistry.getConnectionCount()).isEqualTo(1);

        // when
        sseConnectionRegistry.remove(userId);

        // then
        assertThat(sseConnectionRegistry.getConnectionCount()).isEqualTo(0);
        assertThat(sseConnectionRegistry.getConnectedUserIds()).doesNotContain(userId);
    }

    @Test
    @DisplayName("클라이언트 연결 해제 시 Registry에서 정리한다")
    void shouldCleanupOnClientDisconnect() {
        // given
        Long userId = 1L;
        sseConnectionRegistry.register(userId, 30000L);

        // when
        sseConnectionRegistry.remove(userId);

        // then
        assertThat(sseConnectionRegistry.getConnectionCount()).isEqualTo(0);
        assertThat(sseConnectionRegistry.getConnectedUserIds()).doesNotContain(userId);
    }

    @Test
    @DisplayName("동일 사용자 재연결 시 이전 커넥션을 종료하고 새 커넥션으로 교체한다")
    void shouldReplaceExistingConnectionOnReconnect() {
        // given
        Long userId = 1L;
        SseEmitter firstEmitter = sseConnectionRegistry.register(userId, 30000L);

        // when
        SseEmitter secondEmitter = sseConnectionRegistry.register(userId, 30000L);

        // then
        assertThat(secondEmitter).isNotSameAs(firstEmitter);
        assertThat(sseConnectionRegistry.getConnectionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 사용자의 커넥션을 동시에 관리한다")
    void shouldManageMultipleUserConnections() {
        // given & when
        sseConnectionRegistry.register(1L, 30000L);
        sseConnectionRegistry.register(2L, 30000L);
        sseConnectionRegistry.register(3L, 30000L);

        // then
        assertThat(sseConnectionRegistry.getConnectionCount()).isEqualTo(3);
        assertThat(sseConnectionRegistry.getConnectedUserIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    @DisplayName("특정 사용자에게 이벤트를 전송한다")
    void shouldSendEventToUser() throws IOException {
        // given
        Long userId = 1L;
        sseConnectionRegistry.register(userId, 30000L);
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("test")
                .data("test-data");

        // when & then
        sseConnectionRegistry.sendToUser(userId, event);
    }

    @Test
    @DisplayName("커넥션이 없는 사용자에게 이벤트 전송 시 무시한다")
    void shouldIgnoreEventForDisconnectedUser() {
        // given
        Long userId = 999L;
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .name("test")
                .data("test-data");

        // when & then
        sseConnectionRegistry.sendToUser(userId, event);
        assertThat(sseConnectionRegistry.getConnectionCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 제거 시 예외가 발생하지 않는다")
    void shouldNotThrowWhenRemovingNonExistentUser() {
        // when & then
        sseConnectionRegistry.remove(999L);
        assertThat(sseConnectionRegistry.getConnectionCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("최대 커넥션 수 초과 시 SseConnectionLimitExceededException이 발생한다")
    void shouldThrowWhenMaxConnectionsExceeded() {
        // given
        SseConnectionRegistry limitedRegistry = new SseConnectionRegistry(2);
        limitedRegistry.register(1L, 30000L);
        limitedRegistry.register(2L, 30000L);

        // when & then
        assertThatThrownBy(() -> limitedRegistry.register(3L, 30000L))
                .isInstanceOf(SseConnectionLimitExceededException.class);
    }

    @Test
    @DisplayName("기존 사용자 재연결은 최대 커넥션 수에 영향받지 않는다")
    void shouldAllowReconnectEvenAtMaxConnections() {
        // given
        SseConnectionRegistry limitedRegistry = new SseConnectionRegistry(2);
        limitedRegistry.register(1L, 30000L);
        limitedRegistry.register(2L, 30000L);

        // when - 기존 사용자 재연결은 허용
        SseEmitter emitter = limitedRegistry.register(1L, 30000L);

        // then
        assertThat(emitter).isNotNull();
        assertThat(limitedRegistry.getConnectionCount()).isEqualTo(2);
    }
}
