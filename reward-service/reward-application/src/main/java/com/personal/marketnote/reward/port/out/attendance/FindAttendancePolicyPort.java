package com.personal.marketnote.reward.port.out.attendance;

import com.personal.marketnote.reward.domain.attendance.AttendancePolicy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 출석 정책 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-20
 * @Description 출석 정책 조회 관련 기능을 제공합니다.
 */
public interface FindAttendancePolicyPort {
    /**
     * @param id 출석 정책 식별자
     * @return 출석 정책 {@link AttendancePolicy}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 식별자로 출석 정책을 조회합니다.
     */
    Optional<AttendancePolicy> findById(Short id);

    /**
     * @param continuousPeriod 연속 출석 기간
     * @param attendedDate     출석 날짜
     * @return 출석 정책 {@link AttendancePolicy}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 연속 출석 기간과 출석 날짜로 출석 정책을 조회합니다.
     */
    Optional<AttendancePolicy> findByContinuousPeriodAndAttendenceDate(short continuousPeriod, LocalDate attendedDate);

    /**
     * @param continuousPeriod 연속 출석 기간
     * @return 출석 정책 {@link AttendancePolicy}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 연속 출석 기간으로 출석 날짜가 없는 출석 정책을 조회합니다.
     */
    Optional<AttendancePolicy> findByContinuousPeriodAndAttendenceDateIsNull(short continuousPeriod);

    /**
     * @return 출석 정책 목록 {@link AttendancePolicy}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 모든 출석 정책을 정렬 번호 내림차순으로 조회합니다.
     */
    List<AttendancePolicy> findAllOrderByOrderNumDesc();

    /**
     * @param id 출석 정책 식별자
     * @return 출석 정책 {@link AttendancePolicy}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 식별자로 출석 정책을 수정 잠금과 함께 조회합니다.
     */
    Optional<AttendancePolicy> findByIdForUpdate(Short id);
}

