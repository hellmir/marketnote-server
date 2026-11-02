package com.personal.marketnote.community.domain.report;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class Report {
    private ReportTargetType targetType;
    private Long targetId;
    private Long reporterId;
    private String reason;
    private LocalDateTime createdAt;

    public static Report from(ReportCreateState state) {
        return Report.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .reporterId(state.getReporterId())
                .reason(state.getReason())
                .createdAt(state.getCreatedAt())
                .build();
    }

    public static Report from(ReportSnapshotState state) {
        return Report.builder()
                .targetType(state.getTargetType())
                .targetId(state.getTargetId())
                .reporterId(state.getReporterId())
                .reason(state.getReason())
                .createdAt(state.getCreatedAt())
                .build();
    }
}
