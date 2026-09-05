package com.personal.marketnote.common.kafka.event;

public record PointAccruedEvent(
        Long userId,
        Long amount
) {
}
