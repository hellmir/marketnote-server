package com.personal.marketnote.common.configuration.kafka;

public record DltResolveResult(
        DltResolutionStatus resolution,
        boolean alreadyResolved
) {
}
