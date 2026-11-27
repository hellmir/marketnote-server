package com.personal.marketnote.common.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserSignupCompletedEvent(
    Long userId,
    String userKey,
    String phoneNumber,
    String userName
) {
}
