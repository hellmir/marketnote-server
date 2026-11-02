package com.personal.marketnote.community.port.in.result.report;

import com.personal.marketnote.community.domain.report.Report;

import java.util.List;

public record GetReportsResult(
        List<ReportItemResult> reports
) {
    public static GetReportsResult from(List<Report> reports) {
        return new GetReportsResult(
                reports.stream()
                        .map(ReportItemResult::from)
                        .toList()
        );
    }
}
