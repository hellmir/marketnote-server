package com.personal.marketnote.reward.port.out.point;

import com.personal.marketnote.reward.domain.point.UserPoint;

/**
 * 회원 포인트 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 회원 포인트 저장 기능을 제공합니다.
 */
public interface SaveUserPointPort {
    /**
     * @param userPoint 회원 포인트
     * @return 저장된 회원 포인트 {@link UserPoint}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 포인트를 저장합니다.
     */
    UserPoint save(UserPoint userPoint);
}
