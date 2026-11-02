package com.personal.marketnote.community.domain.report;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportSnapshotState {
    private final ReportTargetType targetType;
    private final Long targetId;
    private final Long reporterId;
    private final String reason;
    private final LocalDateTime createdAt;
}
