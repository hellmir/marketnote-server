package com.personal.marketnote.community.service.report;

import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.exception.PostAlreadyReportedException;
import com.personal.marketnote.community.exception.ReviewAlreadyReportedException;
import com.personal.marketnote.community.port.out.report.FindReportPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateDuplicateReportUseCaseTest {
    @Mock
    private FindReportPort findReportPort;

    @InjectMocks
    private GetReportService getReportService;

    @Test
    @DisplayName("리뷰를 신고한 이력이 없으면 검증을 통과한다")
    void validateDuplicateReport_reviewNotReported_passes() {
        ReportTargetType targetType = ReportTargetType.REVIEW;
        Long targetId = 1L;
        Long reporterId = 100L;
        when(findReportPort.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId))
                .thenReturn(false);

        assertThatCode(() -> getReportService.validateDuplicateReport(targetType, targetId, reporterId))
                .doesNotThrowAnyException();

        verify(findReportPort).existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId);
    }

    @Test
    @DisplayName("리뷰를 이미 신고한 경우 ReviewAlreadyReportedException이 발생한다")
    void validateDuplicateReport_reviewAlreadyReported_throwsReviewAlreadyReportedException() {
        ReportTargetType targetType = ReportTargetType.REVIEW;
        Long targetId = 1L;
        Long reporterId = 100L;
        when(findReportPort.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId))
                .thenReturn(true);

        assertThatThrownBy(() -> getReportService.validateDuplicateReport(targetType, targetId, reporterId))
                .isInstanceOf(ReviewAlreadyReportedException.class)
                .hasMessageContaining(String.valueOf(targetId))
                .hasMessageContaining(String.valueOf(reporterId));
    }

    @Test
    @DisplayName("게시글을 신고한 이력이 없으면 검증을 통과한다")
    void validateDuplicateReport_postNotReported_passes() {
        ReportTargetType targetType = ReportTargetType.POST;
        Long targetId = 2L;
        Long reporterId = 200L;
        when(findReportPort.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId))
                .thenReturn(false);

        assertThatCode(() -> getReportService.validateDuplicateReport(targetType, targetId, reporterId))
                .doesNotThrowAnyException();

        verify(findReportPort).existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId);
    }

    @Test
    @DisplayName("게시글을 이미 신고한 경우 PostAlreadyReportedException이 발생한다")
    void validateDuplicateReport_postAlreadyReported_throwsPostAlreadyReportedException() {
        ReportTargetType targetType = ReportTargetType.POST;
        Long targetId = 2L;
        Long reporterId = 200L;
        when(findReportPort.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId))
                .thenReturn(true);

        assertThatThrownBy(() -> getReportService.validateDuplicateReport(targetType, targetId, reporterId))
                .isInstanceOf(PostAlreadyReportedException.class)
                .hasMessageContaining(String.valueOf(targetId))
                .hasMessageContaining(String.valueOf(reporterId));
    }

    @Test
    @DisplayName("검증 시 FindReportPort에 정확한 파라미터가 전달된다")
    void validateDuplicateReport_passesExactParameters() {
        ReportTargetType targetType = ReportTargetType.REVIEW;
        Long targetId = 999L;
        Long reporterId = 888L;
        when(findReportPort.existsByTargetTypeAndTargetIdAndReporterId(targetType, targetId, reporterId))
                .thenReturn(false);

        getReportService.validateDuplicateReport(targetType, targetId, reporterId);

        verify(findReportPort).existsByTargetTypeAndTargetIdAndReporterId(
                ReportTargetType.REVIEW, 999L, 888L
        );
    }
}
