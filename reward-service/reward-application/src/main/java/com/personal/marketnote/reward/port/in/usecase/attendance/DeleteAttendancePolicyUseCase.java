package com.personal.marketnote.reward.port.in.usecase.attendance;

/**
 * 출석 정책 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-21
 * @Description 출석 정책 삭제 기능을 제공합니다.
 */
public interface DeleteAttendancePolicyUseCase {
    /**
     * @param id 출석 정책 ID
     * @Date 2026-01-21
     * @Author 성효빈
     * @Description 출석 정책을 삭제합니다.
     */
    void delete(Short id);
}

