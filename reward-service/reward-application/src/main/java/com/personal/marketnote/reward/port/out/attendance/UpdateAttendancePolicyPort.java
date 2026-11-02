package com.personal.marketnote.reward.port.out.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;

/**
 * 출석 정책 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 출석 정책 수정 기능을 제공합니다.
 */
public interface UpdateAttendancePolicyPort {
    /**
     * @param attendancePolicy 출석 정책
     * @Date 2026-01-21
     * @Author 성효빈
     * @Description 출석 정책을 수정합니다.
     */
    void update(AttendancePolicy attendancePolicy);
}
