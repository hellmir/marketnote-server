package com.personal.marketnote.community.service.report;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.exception.PostAlreadyReportedException;
import com.personal.marketnote.community.exception.ReviewAlreadyReportedException;
import com.personal.marketnote.community.port.in.usecase.report.GetReportUseCase;
import com.personal.marketnote.community.port.out.report.FindReportPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetReportService implements GetReportUseCase {
    private final FindReportPort findReportPort;

    @Override
    public void validateDuplicateReport(ReportTargetType targetType, Long targetId, Long reporterId) {
        if (findReportPort.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId)) {
            if (targetType.isReview()) {
                throw new ReviewAlreadyReportedException(targetId, reporterId);
            }

            throw new PostAlreadyReportedException(targetId, reporterId);
        }
    }

    @Override
    public List<Report> getReports(ReportTargetType targetType, Long targetId) {
        return findReportPort.findByTargetTypeAndTargetId(targetType, targetId);
    }
}
