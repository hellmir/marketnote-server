package com.personal.marketnote.reward.port.in.usecase.attendance;

import com.personal.marketnote.reward.port.in.command.attendance.GetMonthlyAttendanceQuery;
import com.personal.marketnote.reward.port.in.result.attendance.GetMonthlyAttendanceResult;

/**
 * 월별 출석 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 월별 출석 조회 기능을 제공합니다.
 */
public interface GetMonthlyAttendanceUseCase {
    /**
     * @param query 월별 출석 조회 쿼리
     * @return 월별 출석 이력 {@link GetMonthlyAttendanceResult}
     * @Date 2026-01-21
     * @Author 성효빈
     * @Description 월별 출석 이력을 조회합니다.
     */
    GetMonthlyAttendanceResult getMonthlyAttendanceHistories(GetMonthlyAttendanceQuery query);
}
