package com.personal.marketnote.reward.port.out.attendance;

import com.personal.marketnote.common.domain.calendar.Month;
import com.personal.marketnote.common.domain.calendar.Year;
import com.personal.marketnote.reward.domain.attendance.UserAttendance;

import java.util.Optional;

/**
 * 회원 출석 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 회원 출석 조회 관련 기능을 제공합니다.
 */
public interface FindUserAttendancePort {
    /**
     * @param userId 회원 식별자
     * @param year   연도
     * @param month  월
     * @return 회원 출석 {@link UserAttendance}
     * @Date 2026-01-21
     * @Author 성효빈
     * @Description 회원 식별자, 연도, 월로 회원 출석을 조회합니다.
     */
    Optional<UserAttendance> findByUserIdAndYearAndMonth(Long userId, Year year, Month month);
}

