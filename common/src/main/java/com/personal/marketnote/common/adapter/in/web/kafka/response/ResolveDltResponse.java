package com.personal.marketnote.common.adapter.in.web.kafka.response;

import com.personal.marketnote.common.configuration.kafka.DltResolveCommand;
import com.personal.marketnote.common.configuration.kafka.DltResolveResult;
import com.personal.marketnote.common.kafka.DltTopicRegistry;

public record ResolveDltResponse(
        String originalTopic,
        String dltTopic,
        int partition,
        long offset,
        String resolution,
        String reason,
        boolean alreadyResolved
) {
    public static ResolveDltResponse from(DltResolveCommand command, DltResolveResult result) {
        return new ResolveDltResponse(
                command.originalTopic(),
                DltTopicRegistry.toDltTopic(command.originalTopic()),
                command.partition(),
                command.offset(),
                result.resolution().name(),
                command.reason(),
                result.alreadyResolved()
        );
    }
}
