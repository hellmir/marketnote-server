package com.personal.marketnote.common.kafka.event;

public record InquiryAnsweredEvent(
        Long userId,
        Long postId,
        String title,
        String board
) {
}
