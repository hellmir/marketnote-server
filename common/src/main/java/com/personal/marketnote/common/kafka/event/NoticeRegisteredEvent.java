package com.personal.marketnote.common.kafka.event;

public record NoticeRegisteredEvent(
        Long postId,
        String title
) {
}
