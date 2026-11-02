package com.personal.marketnote.reward.port.out.point;

import com.personal.marketnote.reward.domain.point.UserPointHistory;

/**
 * 회원 포인트 이력 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 회원 포인트 이력 저장 기능을 제공합니다.
 */
public interface SaveUserPointHistoryPort {
    /**
     * @param history 회원 포인트 이력
     * @return 저장된 회원 포인트 이력 {@link UserPointHistory}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 포인트 이력을 저장합니다.
     */
    UserPointHistory save(UserPointHistory history);
}
