package com.personal.marketnote.common.kafka.event;

public record EventRegisteredEvent(
        Long postId,
        String title
) {
}
