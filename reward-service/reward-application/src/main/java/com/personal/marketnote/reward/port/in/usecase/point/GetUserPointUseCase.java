package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.domain.point.UserPoint;

/**
 * 회원 포인트 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 회원 포인트 조회 관련 기능을 제공합니다.
 */
public interface GetUserPointUseCase {
    /**
     * @param userKey 회원 키
     * @return 회원 포인트 존재 여부 {@link boolean}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 포인트가 존재하는지 여부를 확인합니다.
     */
    boolean existsUserPoint(String userKey);

    /**
     * @param userId 회원 ID
     * @return 회원 포인트 정보
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 포인트 정보를 조회합니다.
     */
    UserPoint getUserPoint(Long userId);

    /**
     * @param userKey 회원 키
     * @return 회원 포인트 정보
     * @Date 2026-01-19
     * @Author 성효빈
     * @Description 회원 포인트 정보를 조회합니다.
     */
    UserPoint getUserPoint(String userKey);
}
