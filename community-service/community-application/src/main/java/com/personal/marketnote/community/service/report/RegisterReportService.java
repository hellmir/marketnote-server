package com.personal.marketnote.community.service.report;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.mapper.ReportCommandToStateMapper;
import com.personal.marketnote.community.port.in.command.report.ReportCommand;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.in.usecase.report.GetReportUseCase;
import com.personal.marketnote.community.port.in.usecase.report.RegisterReportUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.report.SaveReportPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterReportService implements RegisterReportUseCase {
    private final GetReportUseCase getReportUseCase;
    private final GetReviewUseCase getReviewUseCase;
    private final GetPostUseCase getPostUseCase;
    private final SaveReportPort saveReportPort;

    @Override
    public void report(ReportCommand command) {
        validate(command);
        saveReportPort.save(
                Report.from(ReportCommandToStateMapper.mapToState(command))
        );
    }

    private void validate(ReportCommand command) {
        ReportTargetType targetType = command.targetType();
        Long targetId = command.targetId();
        Long reporterId = command.reporterId();

        getReportUseCase.validateDuplicateReport(targetType, targetId, reporterId);

        if (command.isReviewReport()) {
            if (!getReviewUseCase.existsReview(targetId)) {
                log.warn("리뷰를 찾을 수 없습니다. reviewId={}", targetId);
                throw new ReviewNotFoundException();
            }

            return;
        }

        if (!getPostUseCase.existsPost(targetId)) {
            log.warn("게시글을 찾을 수 없습니다. postId={}", targetId);
            throw new PostNotFoundException();
        }
    }
}
