package com.personal.marketnote.community.adapter.out.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.adapter.out.persistence.report.entity.ReportJpaEntity;
import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportSnapshotState;

import java.util.Optional;

public class ReportJpaEntityToDomainMapper {
    public static Optional<Report> mapToDomain(ReportJpaEntity entity) {
        if (FormatValidator.hasNoValue(entity)) {
            return Optional.empty();
        }

        return Optional.of(
                Report.from(
                        ReportSnapshotState.builder()
                                .targetType(entity.getId().getTargetType())
                                .targetId(entity.getTargetId())
                                .reporterId(entity.getReporterId())
                                .reason(entity.getReason())
                                .createdAt(entity.getCreatedAt())
                                .build()
                )
        );
    }
}
