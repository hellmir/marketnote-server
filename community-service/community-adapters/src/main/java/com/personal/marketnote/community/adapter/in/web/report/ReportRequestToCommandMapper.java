package com.personal.marketnote.community.adapter.in.web.report;

import com.personal.marketnote.community.adapter.in.web.report.request.RegisterReportRequest;
import com.personal.marketnote.community.adapter.in.web.report.request.UpdateTargetStatusRequest;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.port.in.command.report.ReportCommand;
import com.personal.marketnote.community.port.in.command.report.UpdateTargetStatusCommand;

public class ReportRequestToCommandMapper {
    public static ReportCommand mapToCommand(
            ReportTargetType targetType, Long targetId, Long userId, RegisterReportRequest request
    ) {
        return ReportCommand.builder()
                .targetType(targetType)
                .targetId(targetId)
                .reporterId(userId)
                .reason(request.getReason())
                .build();
    }

    public static UpdateTargetStatusCommand mapToCommand(UpdateTargetStatusRequest request) {
        return UpdateTargetStatusCommand.of(request.targetType(), request.targetId(), request.isVisible());
    }
}
