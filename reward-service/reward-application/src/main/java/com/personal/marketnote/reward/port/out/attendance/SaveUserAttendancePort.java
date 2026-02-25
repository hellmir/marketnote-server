package com.personal.marketnote.reward.port.out.attendance;

import com.personal.marketnote.reward.domain.attendance.UserAttendance;

/**
 * 회원 출석 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 회원 출석 저장 기능을 제공합니다.
 */
public interface SaveUserAttendancePort {
    /**
     * @param attendance 회원 출석
     * @return 저장된 회원 출석 {@link UserAttendance}
     * @Date 2026-01-21
     * @Author 성효빈
     * @Description 회원 출석을 저장합니다.
     */
    UserAttendance save(UserAttendance attendance);
}

