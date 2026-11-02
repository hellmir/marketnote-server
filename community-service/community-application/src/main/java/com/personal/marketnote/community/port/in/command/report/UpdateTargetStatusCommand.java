package com.personal.marketnote.community.port.in.command.report;

import com.personal.marketnote.community.domain.report.ReportTargetType;
import lombok.Builder;

@Builder
public record UpdateTargetStatusCommand(
        ReportTargetType targetType,
        Long targetId,
        boolean isVisible
) {
    public static UpdateTargetStatusCommand of(
            ReportTargetType targetType,
            Long targetId,
            boolean isVisible
    ) {
        return new UpdateTargetStatusCommand(targetType, targetId, isVisible);
    }

    public boolean isReview() {
        return targetType.isReview();
    }
}
