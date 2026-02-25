package com.personal.marketnote.reward.port.out.attendance;

import com.personal.marketnote.reward.domain.attendance.UserAttendanceHistory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 회원 출석 이력 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-20
 * @Description 회원 출석 이력 조회 관련 기능을 제공합니다.
 */
public interface FindUserAttendanceHistoryPort {
    /**
     * @param userAttendanceId 회원 출석 식별자
     * @return 가장 최근 회원 출석 이력 {@link UserAttendanceHistory}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 회원 출석 식별자로 가장 최근 출석 이력을 조회합니다.
     */
    Optional<UserAttendanceHistory> findLatestByUserAttendanceId(Long userAttendanceId);

    /**
     * @param userAttendanceId 회원 출석 식별자
     * @param startInclusive   조회 시작 일시 (포함)
     * @param endExclusive     조회 종료 일시 (미포함)
     * @return 해당 기간 내 출석 이력 존재 여부
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 회원 출석 식별자와 기간으로 출석 이력 존재 여부를 확인합니다.
     */
    boolean existsByUserAttendanceIdAndAttendedAtBetween(Long userAttendanceId, LocalDateTime startInclusive, LocalDateTime endExclusive);
}

