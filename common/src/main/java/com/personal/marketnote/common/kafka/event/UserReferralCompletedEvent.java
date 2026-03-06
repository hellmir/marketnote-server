package com.personal.marketnote.common.kafka.event;

public record UserReferralCompletedEvent(
        Long requestUserId,
        Long referredUserId
) {
}
