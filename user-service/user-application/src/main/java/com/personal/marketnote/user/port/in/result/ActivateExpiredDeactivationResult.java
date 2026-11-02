package com.personal.marketnote.user.port.in.result;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PRIVATE)
public record ActivateExpiredDeactivationResult(
        int activatedCount,
        List<Long> activatedUserIds
) {
    public static ActivateExpiredDeactivationResult of(List<Long> activatedUserIds) {
        return ActivateExpiredDeactivationResult.builder()
                .activatedCount(activatedUserIds.size())
                .activatedUserIds(activatedUserIds)
                .build();
    }
}
