package com.personal.marketnote.community.adapter.out.persistence.report;

import com.personal.marketnote.community.adapter.out.mapper.ReportJpaEntityToDomainMapper;
import com.personal.marketnote.community.adapter.out.persistence.report.entity.ReportJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.report.repository.ReportJpaRepository;
import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.port.out.report.FindReportPort;
import com.personal.marketnote.community.port.out.report.SaveReportPort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@com.personal.marketnote.common.adapter.out.PersistenceAdapter
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements SaveReportPort, FindReportPort {
    private final ReportJpaRepository reportJpaRepository;

    @Override
    public void save(Report report) {
        reportJpaRepository.save(ReportJpaEntity.from(report));
    }

    @Override
    public boolean existsByTargetTypeAndTargetIdAndReporterId(
            ReportTargetType targetType, Long targetId, Long reporterId
    ) {
        return reportJpaRepository.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId);
    }

    @Override
    public List<Report> findByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId) {
        return reportJpaRepository.findByTargetTypeAndTargetId(targetType, targetId)
                .stream()
                .map(
                        reportJpaEntity -> ReportJpaEntityToDomainMapper.mapToDomain(reportJpaEntity)
                                .orElse(null)
                )
                .toList();
    }
}
