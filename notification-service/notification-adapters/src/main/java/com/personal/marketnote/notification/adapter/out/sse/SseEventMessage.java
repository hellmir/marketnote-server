package com.personal.marketnote.notification.adapter.out.sse;

public record SseEventMessage(
        Long userId,
        String eventType,
        String payload
) {
}
