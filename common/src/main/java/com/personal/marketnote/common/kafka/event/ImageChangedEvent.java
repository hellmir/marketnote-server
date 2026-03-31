package com.personal.marketnote.common.kafka.event;

public record ImageChangedEvent(
        Long imageId,
        Long targetId,
        String targetType,
        String imageUrl,
        Integer sortOrder,
        ImageChangeAction action
) {
}
