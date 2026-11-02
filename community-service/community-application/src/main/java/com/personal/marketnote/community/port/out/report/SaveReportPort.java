package com.personal.marketnote.community.port.out.report;

import com.personal.marketnote.community.domain.report.Report;

/**
 * 신고 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 신고 저장 기능을 제공합니다.
 */
public interface SaveReportPort {
    /**
     * @param report 신고
     * @Date 2026-01-13
     * @Author 성효빈
     * @Description 신고를 저장합니다.
     */
    void save(Report report);
}
