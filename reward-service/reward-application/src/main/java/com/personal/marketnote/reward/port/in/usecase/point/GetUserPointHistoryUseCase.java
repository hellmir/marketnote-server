package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;
import com.personal.marketnote.reward.port.in.result.point.GetUserPointHistoryResult;

/**
 * 회원 포인트 이력 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-19
 * @Description 회원 포인트 이력 조회 기능을 제공합니다.
 */
public interface GetUserPointHistoryUseCase {
    /**
     * @param userId 회원 ID
     * @param filter 포인트 이력 필터
     * @return 회원 포인트 이력 {@link GetUserPointHistoryResult}
     * @Date 2026-01-19
     * @Author 성효빈
     * @Description 회원 포인트 이력을 조회합니다.
     */
    GetUserPointHistoryResult getUserPointHistories(Long userId, UserPointHistoryFilter filter);
}

