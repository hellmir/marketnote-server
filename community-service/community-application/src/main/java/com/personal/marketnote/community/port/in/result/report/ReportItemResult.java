package com.personal.marketnote.community.port.in.result.report;

import com.personal.marketnote.community.domain.report.Report;

import java.time.LocalDateTime;

public record ReportItemResult(
        Long targetId,
        Long reporterId,
        String reason,
        LocalDateTime createdAt
) {
    public static ReportItemResult from(Report report) {
        return new ReportItemResult(
                report.getTargetId(),
                report.getReporterId(),
                report.getReason(),
                report.getCreatedAt()
        );
    }
}
