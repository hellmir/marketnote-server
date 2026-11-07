package com.personal.marketnote.community.mapper;

import com.personal.marketnote.community.domain.report.ReportCreateState;
import com.personal.marketnote.community.port.in.command.report.ReportCommand;

public class ReportCommandToStateMapper {
    private ReportCommandToStateMapper() {
    }

    public static ReportCreateState mapToState(ReportCommand command) {
        return ReportCreateState.builder()
                .targetType(command.targetType())
                .targetId(command.targetId())
                .reporterId(command.reporterId())
                .reason(command.reason())
                .build();
    }
}
