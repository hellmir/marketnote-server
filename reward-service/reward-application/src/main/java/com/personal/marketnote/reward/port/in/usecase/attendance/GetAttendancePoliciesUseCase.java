package com.personal.marketnote.reward.port.in.usecase.attendance;

import com.personal.marketnote.reward.port.in.result.attendance.GetAttendancePoliciesResult;

/**
 * 출석 정책 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 출석 정책 조회 관련 기능을 제공합니다.
 */
public interface GetAttendancePoliciesUseCase {
    /**
     * @return 출석 정책 목록 {@link GetAttendancePoliciesResult}
     * @Date 2026-01-21
     * @Author 성효빈
     * @Description 출석 정책 목록을 조회합니다.
     */
    GetAttendancePoliciesResult getAttendancePolicies();
}

