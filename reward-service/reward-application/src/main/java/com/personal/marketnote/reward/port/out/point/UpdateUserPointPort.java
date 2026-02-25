package com.personal.marketnote.reward.port.out.point;

import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;

/**
 * 회원 포인트 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-18
 * @Description 회원 포인트 수정 기능을 제공합니다.
 */
public interface UpdateUserPointPort {
    /**
     * @param userPoint 회원 포인트
     * @return 수정된 회원 포인트 {@link UserPoint}
     * @Date 2026-01-18
     * @Author 성효빈
     * @Description 회원 포인트를 수정합니다.
     */
    UserPoint update(UserPoint userPoint) throws UserPointNotFoundException;
}
