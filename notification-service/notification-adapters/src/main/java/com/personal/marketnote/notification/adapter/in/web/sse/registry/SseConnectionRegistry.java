package com.personal.marketnote.notification.adapter.in.web.sse.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SseConnectionRegistry {

    private final ConcurrentHashMap<Long, SseEmitter> connections = new ConcurrentHashMap<>();
    private final int maxConnections;

    public SseConnectionRegistry(
            @Value("${sse.max-connections:5000}") int maxConnections
    ) {
        this.maxConnections = maxConnections;
    }

    public SseEmitter register(Long userId, long timeoutMs) {
        if (!connections.containsKey(userId) && connections.size() >= maxConnections) {
            log.warn("SSE 최대 커넥션 수 초과: maxConnections={}", maxConnections);
            throw new SseConnectionLimitExceededException(maxConnections);
        }

        SseEmitter emitter = new SseEmitter(timeoutMs);

        emitter.onCompletion(() -> {
            connections.remove(userId, emitter);
            log.debug("SSE 커넥션 완료: userId={}", userId);
        });

        emitter.onTimeout(() -> {
            connections.remove(userId, emitter);
            log.debug("SSE 커넥션 타임아웃: userId={}", userId);
        });

        emitter.onError(throwable -> {
            connections.remove(userId, emitter);
            log.debug("SSE 커넥션 에러: userId={}", userId, throwable);
        });

        SseEmitter previousEmitter = connections.put(userId, emitter);
        if (previousEmitter != null) {
            log.info("SSE 기존 커넥션 종료: userId={}", userId);
            previousEmitter.complete();
        }

        log.info("SSE 커넥션 등록: userId={}, timeout={}ms", userId, timeoutMs);
        sendConnectEvent(emitter, userId);

        return emitter;
    }

    public void remove(Long userId) {
        SseEmitter emitter = connections.remove(userId);
        if (emitter != null) {
            emitter.complete();
            log.info("SSE 커넥션 제거: userId={}", userId);
        }
    }

    public void sendToUser(Long userId, SseEmitter.SseEventBuilder event) {
        SseEmitter emitter = connections.get(userId);
        if (emitter == null) {
            return;
        }

        try {
            emitter.send(event);
        } catch (IOException exception) {
            connections.remove(userId, emitter);
            emitter.completeWithError(exception);
            log.warn("SSE 이벤트 전송 실패 (IO), 커넥션 제거: userId={}", userId);
        } catch (IllegalStateException exception) {
            connections.remove(userId, emitter);
            log.warn("SSE 이벤트 전송 실패 (emitter 비활성), 커넥션 제거: userId={}", userId);
        }
    }

    public Set<Long> getConnectedUserIds() {
        return Set.copyOf(connections.keySet());
    }

    public int getConnectionCount() {
        return connections.size();
    }

    private void sendConnectEvent(SseEmitter emitter, Long userId) {
        try {
            emitter.send(SseEmitter.event().name("connect").data("CONNECTED"));
        } catch (IOException exception) {
            connections.remove(userId, emitter);
            emitter.completeWithError(exception);
            log.warn("SSE 초기 연결 이벤트 전송 실패: userId={}", userId);
        }
    }
}
