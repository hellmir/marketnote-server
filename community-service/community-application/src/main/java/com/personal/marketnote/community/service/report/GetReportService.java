package com.personal.marketnote.community.service.report;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.exception.PostAlreadyReportedException;
import com.personal.marketnote.community.exception.ReviewAlreadyReportedException;
import com.personal.marketnote.community.port.in.usecase.report.GetReportUseCase;
import com.personal.marketnote.community.port.out.report.FindReportPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetReportService implements GetReportUseCase {
    private final FindReportPort findReportPort;

    @Override
    public void validateDuplicateReport(ReportTargetType targetType, Long targetId, Long reporterId) {
        if (findReportPort.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId)) {
            if (targetType.isReview()) {
                log.warn("이미 해당 리뷰를 신고했습니다. reviewId={}, reporterId={}", targetId, reporterId);
                throw new ReviewAlreadyReportedException();
            }

            log.warn("이미 해당 게시글을 신고했습니다. postId={}, reporterId={}", targetId, reporterId);
            throw new PostAlreadyReportedException();
        }
    }

    @Override
    public List<Report> getReports(ReportTargetType targetType, Long targetId) {
        return findReportPort.findByTargetTypeAndTargetId(targetType, targetId);
    }
}
