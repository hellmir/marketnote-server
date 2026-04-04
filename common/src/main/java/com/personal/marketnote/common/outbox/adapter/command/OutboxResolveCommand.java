package com.personal.marketnote.common.outbox.adapter.command;

import com.personal.marketnote.common.outbox.OutboxResolutionAction;

public record OutboxResolveCommand(
        Long id,
        OutboxResolutionAction action,
        String reason
) {
}
