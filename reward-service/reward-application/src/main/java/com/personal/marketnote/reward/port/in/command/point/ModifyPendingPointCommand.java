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

    public static ModifyPendingPointCommand from(ModifyPendingSharedPointCommand source, Long userId) {
        return ModifyPendingPointCommand.builder()
                .userId(userId)
                .changeType(source.changeType())
                .amount(source.amount())
                .sourceType(source.sourceType())
                .sourceId(source.sourceId())
                .reason(source.reason())
                .build();
    }
}
