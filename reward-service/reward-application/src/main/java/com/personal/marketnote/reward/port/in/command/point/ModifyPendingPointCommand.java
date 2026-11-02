package com.personal.marketnote.reward.port.in.command.point;

import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import lombok.Builder;

@Builder
public record ModifyPendingPointCommand(
        Long userId,
        UserPointChangeType changeType,
        Long amount,
        UserPointSourceType sourceType,
        Long sourceId,
        String reason
) {
    public boolean isAccrual() {
        return changeType.isAccrual();
    }
}
