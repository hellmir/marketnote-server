package com.personal.marketnote.common.outbox.adapter.in.web.response;

import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveCommand;
import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveResult;

public record ResolveOutboxResponse(
        Long id,
        String eventId,
        String topic,
        String resolution,
        String reason,
        boolean alreadyResolved
) {
    public static ResolveOutboxResponse from(OutboxResolveCommand command, OutboxResolveResult result) {
        return new ResolveOutboxResponse(
                command.id(),
                result.eventId(),
                result.topic(),
                result.resolution(),
                result.reason(),
                result.alreadyResolved()
        );
    }
}
