package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NoticeRegisteredEvent(
        Long postId,
        String title,
        boolean isImportant
) {
}
