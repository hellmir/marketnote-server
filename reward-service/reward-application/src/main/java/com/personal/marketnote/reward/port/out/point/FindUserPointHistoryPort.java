package com.personal.marketnote.reward.port.out.point;

import com.personal.marketnote.reward.domain.point.UserPointHistory;
import com.personal.marketnote.reward.domain.point.UserPointHistoryFilter;

import java.util.List;

/**
 * 회원 포인트 이력 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-19
 * @Description 회원 포인트 이력 조회 관련 기능을 제공합니다.
 */
public interface FindUserPointHistoryPort {
    /**
     * @param userId 회원 식별자
     * @param filter 포인트 이력 필터
     * @return 회원 포인트 이력 목록 {@link UserPointHistory}
     * @Date 2026-01-19
     * @Author 성효빈
     * @Description 회원 식별자와 필터 조건으로 포인트 이력을 조회합니다.
     */
    List<UserPointHistory> findByUserId(Long userId, UserPointHistoryFilter filter);
}

