package com.personal.marketnote.reward.port.out.attendance;

import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;

/**
 * 회원 출석 이력 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-20
 * @Description 회원 출석 이력 저장 기능을 제공합니다.
 */
public interface SaveUserAttendanceHistoryPort {
    /**
     * @param history 회원 출석 이력
     * @return 저장된 회원 출석 이력 {@link UserAttendanceHistory}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 회원 출석 이력을 저장합니다.
     */
    UserAttendanceHistory save(UserAttendanceHistory history);
}

