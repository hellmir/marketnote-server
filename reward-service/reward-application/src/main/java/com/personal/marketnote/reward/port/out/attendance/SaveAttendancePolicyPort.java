package com.personal.marketnote.reward.port.out.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;

/**
 * 출석 정책 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 출석 정책 저장 기능을 제공합니다.
 */
public interface SaveAttendancePolicyPort {
    /**
     * @param attendancePolicy 출석 정책
     * @return 저장된 출석 정책 {@link AttendancePolicy}
     * @Date 2026-01-21
     * @Author 성효빈
     * @Description 출석 정책을 저장합니다.
     */
    AttendancePolicy save(AttendancePolicy attendancePolicy);
}

