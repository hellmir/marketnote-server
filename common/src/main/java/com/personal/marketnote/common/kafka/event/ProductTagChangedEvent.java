package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductTagChangedEvent(
    Long productId,
    List<ProductTagSnapshot> productTags,
    Action action
) {

    public enum Action {
        UPDATED,
        REORDERED
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductTagSnapshot(
        Long id,
        String name,
        Long orderNum
    ) {
    }
}
