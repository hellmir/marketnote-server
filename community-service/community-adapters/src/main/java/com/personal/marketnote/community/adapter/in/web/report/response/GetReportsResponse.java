package com.personal.marketnote.community.adapter.in.web.report.response;

import com.personal.marketnote.community.port.in.result.report.GetReportsResult;
import com.personal.marketnote.community.port.in.result.report.ReportItemResult;

import java.util.List;

public record GetReportsResponse(
        List<ReportItemResult> reports
) {
    public static GetReportsResponse from(GetReportsResult result) {
        return new GetReportsResponse(result.reports());
    }
}
