package com.personal.marketnote.notification.adapter.in.web.sse.scheduler;

import com.personal.marketnote.notification.adapter.in.web.sse.registry.SseConnectionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseHeartbeatSchedulerTest {

    @InjectMocks
    private SseHeartbeatScheduler sseHeartbeatScheduler;

    @Mock
    private SseConnectionRegistry sseConnectionRegistry;

    @Test
    @DisplayName("연결된 모든 사용자에게 heartbeat를 전송한다")
    void shouldSendHeartbeatToAllConnectedUsers() {
        // given
        when(sseConnectionRegistry.getConnectedUserIds()).thenReturn(Set.of(1L, 2L, 3L));

        // when
        sseHeartbeatScheduler.sendHeartbeat();

        // then
        verify(sseConnectionRegistry).sendToUser(eq(1L), any(SseEmitter.SseEventBuilder.class));
        verify(sseConnectionRegistry).sendToUser(eq(2L), any(SseEmitter.SseEventBuilder.class));
        verify(sseConnectionRegistry).sendToUser(eq(3L), any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("연결된 사용자가 없으면 heartbeat를 전송하지 않는다")
    void shouldNotSendHeartbeatWhenNoConnections() {
        // given
        when(sseConnectionRegistry.getConnectedUserIds()).thenReturn(Set.of());

        // when
        sseHeartbeatScheduler.sendHeartbeat();

        // then
        verify(sseConnectionRegistry, never()).sendToUser(any(), any(SseEmitter.SseEventBuilder.class));
    }
}
