package com.personal.marketnote.notification.adapter.in.web.sse.scheduler;

import com.personal.marketnote.notification.adapter.in.web.sse.registry.SseConnectionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseHeartbeatScheduler {

    private final SseConnectionRegistry sseConnectionRegistry;

    @Scheduled(fixedDelayString = "${sse.heartbeat.interval-ms:30000}")
    public void sendHeartbeat() {
        Set<Long> connectedUserIds = sseConnectionRegistry.getConnectedUserIds();
        if (connectedUserIds.isEmpty()) {
            return;
        }

        log.debug("SSE heartbeat 전송 시작: 커넥션 수={}", connectedUserIds.size());

        for (Long userId : connectedUserIds) {
            SseEmitter.SseEventBuilder heartbeatEvent = SseEmitter.event().comment("heartbeat");
            sseConnectionRegistry.sendToUser(userId, heartbeatEvent);
        }

        log.debug("SSE heartbeat 전송 완료: 커넥션 수={}", connectedUserIds.size());
    }
}
