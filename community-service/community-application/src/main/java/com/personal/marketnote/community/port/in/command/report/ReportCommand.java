package com.personal.marketnote.community.port.in.command.report;

import com.personal.marketnote.community.domain.report.ReportTargetType;
import lombok.Builder;

@Builder
public record ReportCommand(
        ReportTargetType targetType,
        Long targetId,
        Long reporterId,
        String reason
) {
    public static ReportCommand of(
            ReportTargetType reportTargetType,
            Long targetId,
            Long reporterId,
            String reason
    ) {
        return new ReportCommand(reportTargetType, targetId, reporterId, reason);
    }

    public boolean isReviewReport() {
        return targetType.isReview();
    }
}
