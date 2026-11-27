package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InquiryAnsweredEvent(
    Long userId,
    Long postId,
    String title,
    String board
) {
}
