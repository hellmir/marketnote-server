package com.personal.marketnote.common.kafka.event;

public record UserSignupCompletedEvent(
        Long userId,
        String userKey
) {
}
