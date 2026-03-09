package com.personal.marketnote.community.service.report;

import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;
import com.personal.marketnote.community.port.out.report.FindReportPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReportsUseCaseTest {
    @Mock
    private FindReportPort findReportPort;

    @InjectMocks
    private GetReportService getReportService;

    @Test
    @DisplayName("신고 내역이 존재하면 리스트를 반환한다")
    void getReports_reportsExist_returnsReportList() {
        ReportTargetType targetType = ReportTargetType.REVIEW;
        Long targetId = 1L;
        List<Report> reports = List.of(
                Report.of(targetType, targetId, 100L, "부적절한 콘텐츠", LocalDateTime.now()),
                Report.of(targetType, targetId, 200L, "스팸 리뷰", LocalDateTime.now())
        );
        when(findReportPort.findByTargetTypeAndTargetId(targetType, targetId))
                .thenReturn(reports);

        List<Report> result = getReportService.getReports(targetType, targetId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReporterId()).isEqualTo(100L);
        assertThat(result.get(1).getReporterId()).isEqualTo(200L);
        verify(findReportPort).findByTargetTypeAndTargetId(targetType, targetId);
    }

    @Test
    @DisplayName("신고 내역이 없으면 빈 리스트를 반환한다")
    void getReports_noReports_returnsEmptyList() {
        ReportTargetType targetType = ReportTargetType.POST;
        Long targetId = 2L;
        when(findReportPort.findByTargetTypeAndTargetId(targetType, targetId))
                .thenReturn(List.of());

        List<Report> result = getReportService.getReports(targetType, targetId);

        assertThat(result).isEmpty();
        verify(findReportPort).findByTargetTypeAndTargetId(targetType, targetId);
    }
}
