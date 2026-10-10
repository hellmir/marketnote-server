package com.personal.marketnote.common.kafka.event;

import java.util.List;

public record ImageChangedEvent(
        Long imageId,
        Long targetId,
        String targetType,
        String fileSort,
        String imageUrl,
        Integer sortOrder,
        List<ResizedImageInfo> resizedImages,
        ImageChangeAction action
) {
    public record ResizedImageInfo(String size, String storageUrl) {
    }
}
