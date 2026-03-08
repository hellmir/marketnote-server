package com.personal.marketnote.reward.port.in.command.point;

import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import lombok.Builder;

@Builder
public record CancelPendingPointCommand(
        Long userId,
        UserPointSourceType sourceType,
        Long sourceId,
        String reason
) {
}
