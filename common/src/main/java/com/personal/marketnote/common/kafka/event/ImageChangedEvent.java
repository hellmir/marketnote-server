package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResizedImageInfo(String size, String storageUrl) {
    }
}
