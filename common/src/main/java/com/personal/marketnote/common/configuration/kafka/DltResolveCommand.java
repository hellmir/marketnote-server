package com.personal.marketnote.common.configuration.kafka;

public record DltResolveCommand(
        String originalTopic,
        int partition,
        long offset,
        DltResolutionAction action,
        String reason
) {
}
