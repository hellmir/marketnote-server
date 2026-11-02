package com.personal.marketnote.community.port.out.report;

import com.personal.marketnote.community.domain.report.Report;
import com.personal.marketnote.community.domain.report.ReportTargetType;

import java.util.List;

/**
 * 신고 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 신고 내역 조회 관련 기능을 제공합니다.
 */
public interface FindReportPort {
    /**
     * @param targetType 대상 유형
     * @param targetId   대상 ID
     * @param reporterId 신고자 ID
     * @return 신고 여부 {@link boolean}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 신고 여부를 조회합니다.
     */
    boolean existsByTargetTypeAndTargetIdAndReporterId(ReportTargetType targetType, Long targetId, Long reporterId);

    /**
     * @param targetType 대상 유형
     * @param targetId   대상 ID
     * @return 신고 내역 {@link List<Report>}
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 신고 내역을 조회합니다.
     */
    List<Report> findByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);
}
