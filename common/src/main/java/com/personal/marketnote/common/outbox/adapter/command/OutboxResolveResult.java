package com.personal.marketnote.common.outbox.adapter.command;

public record OutboxResolveResult(
        String eventId,
        String topic,
        String resolution,
        String reason,
        boolean alreadyResolved
) {
}
