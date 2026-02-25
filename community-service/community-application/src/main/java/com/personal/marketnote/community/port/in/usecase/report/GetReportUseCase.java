package com.personal.marketnote.community.port.in.usecase.report;

import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;

import java.util.List;

/**
 * 신고 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 신고 내역 조회 관련 기능을 제공합니다.
 */
public interface GetReportUseCase {
    /**
     * @param targetType 대상 유형
     * @param targetId   대상 ID
     * @param reporterId 신고자 ID
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 중복 신고 여부를 검증합니다.
     */
    void validateDuplicateReport(ReportTargetType targetType, Long targetId, Long reporterId);


    /**
     * @param targetType 대상 유형
     * @param targetId   대상 ID
     * @return 신고 내역 조회 결과 {@link List<Report>}
     * @Date 2026-01-13
     * @Author 성효빈
     * @Description 신고 내역을 조회합니다.
     */
    List<Report> getReports(ReportTargetType targetType, Long targetId);
}
