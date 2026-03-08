package com.personal.marketnote.community.service.report;

import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.exception.PostAlreadyReportedException;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.exception.ReviewAlreadyReportedException;
import com.personal.marketnote.community.exception.ReviewNotFoundException;
import com.personal.marketnote.community.port.in.command.report.ReportCommand;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.in.usecase.report.GetReportUseCase;
import com.personal.marketnote.community.port.in.usecase.review.GetReviewUseCase;
import com.personal.marketnote.community.port.out.report.SaveReportPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterReportUseCaseTest {
    @Mock
    private GetReportUseCase getReportUseCase;
    @Mock
    private GetReviewUseCase getReviewUseCase;
    @Mock
    private GetPostUseCase getPostUseCase;
    @Mock
    private SaveReportPort saveReportPort;

    @InjectMocks
    private RegisterReportService registerReportService;

    @Test
    @DisplayName("리뷰 신고 시 중복 검증 통과 후 저장에 성공한다")
    void report_reviewReport_savesSuccessfully() {
        ReportCommand command = buildReviewReportCommand(1L, 100L);
        when(getReviewUseCase.existsReview(1L)).thenReturn(true);

        registerReportService.report(command);

        verify(getReportUseCase).validateDuplicateReport(ReportTargetType.REVIEW, 1L, 100L);
        verify(getReviewUseCase).existsReview(1L);
        verify(saveReportPort).save(any(Report.class));
        verifyNoInteractions(getPostUseCase);
    }

    @Test
    @DisplayName("게시글 신고 시 중복 검증 통과 후 저장에 성공한다")
    void report_postReport_savesSuccessfully() {
        ReportCommand command = buildPostReportCommand(2L, 200L);
        when(getPostUseCase.existsPost(2L)).thenReturn(true);

        registerReportService.report(command);

        verify(getReportUseCase).validateDuplicateReport(ReportTargetType.POST, 2L, 200L);
        verify(getPostUseCase).existsPost(2L);
        verify(saveReportPort).save(any(Report.class));
        verifyNoInteractions(getReviewUseCase);
    }

    @Test
    @DisplayName("중복 리뷰 신고 시 ReviewAlreadyReportedException이 발생한다")
    void report_duplicateReviewReport_throwsReviewAlreadyReportedException() {
        ReportCommand command = buildReviewReportCommand(1L, 100L);
        doThrow(new ReviewAlreadyReportedException(1L, 100L))
                .when(getReportUseCase).validateDuplicateReport(ReportTargetType.REVIEW, 1L, 100L);

        assertThatThrownBy(() -> registerReportService.report(command))
                .isInstanceOf(ReviewAlreadyReportedException.class);

        verifyNoInteractions(saveReportPort);
        verifyNoInteractions(getReviewUseCase);
    }

    @Test
    @DisplayName("중복 게시글 신고 시 PostAlreadyReportedException이 발생한다")
    void report_duplicatePostReport_throwsPostAlreadyReportedException() {
        ReportCommand command = buildPostReportCommand(2L, 200L);
        doThrow(new PostAlreadyReportedException(2L, 200L))
                .when(getReportUseCase).validateDuplicateReport(ReportTargetType.POST, 2L, 200L);

        assertThatThrownBy(() -> registerReportService.report(command))
                .isInstanceOf(PostAlreadyReportedException.class);

        verifyNoInteractions(saveReportPort);
        verifyNoInteractions(getPostUseCase);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰를 신고하면 ReviewNotFoundException이 발생한다")
    void report_reviewNotFound_throwsReviewNotFoundException() {
        ReportCommand command = buildReviewReportCommand(999L, 100L);
        when(getReviewUseCase.existsReview(999L)).thenReturn(false);

        assertThatThrownBy(() -> registerReportService.report(command))
                .isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(saveReportPort);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 신고하면 PostNotFoundException이 발생한다")
    void report_postNotFound_throwsPostNotFoundException() {
        ReportCommand command = buildPostReportCommand(999L, 200L);
        when(getPostUseCase.existsPost(999L)).thenReturn(false);

        assertThatThrownBy(() -> registerReportService.report(command))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("999");

        verifyNoInteractions(saveReportPort);
    }

    @Test
    @DisplayName("리뷰 신고 성공 시 SaveReportPort.save()에 올바른 Report가 전달된다")
    void report_reviewReport_passesCorrectReportToSavePort() {
        ReportCommand command = ReportCommand.of(ReportTargetType.REVIEW, 10L, 500L, "부적절한 콘텐츠");
        when(getReviewUseCase.existsReview(10L)).thenReturn(true);

        registerReportService.report(command);

        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(saveReportPort).save(reportCaptor.capture());
        Report captured = reportCaptor.getValue();

        assertThat(captured.getTargetType()).isEqualTo(ReportTargetType.REVIEW);
        assertThat(captured.getTargetId()).isEqualTo(10L);
        assertThat(captured.getReporterId()).isEqualTo(500L);
        assertThat(captured.getReason()).isEqualTo("부적절한 콘텐츠");
    }

    private ReportCommand buildReviewReportCommand(Long targetId, Long reporterId) {
        return ReportCommand.of(ReportTargetType.REVIEW, targetId, reporterId, "부적절한 리뷰입니다");
    }

    private ReportCommand buildPostReportCommand(Long targetId, Long reporterId) {
        return ReportCommand.of(ReportTargetType.POST, targetId, reporterId, "부적절한 게시글입니다");
    }
}
