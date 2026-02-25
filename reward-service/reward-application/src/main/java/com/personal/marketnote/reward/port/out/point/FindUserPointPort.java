package com.personal.marketnote.reward.port.out.point;

import com.personal.marketnote.reward.domain.point.UserPoint;

import java.util.Optional;

/**
 * 회원 포인트 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 회원 포인트 조회 관련 기능을 제공합니다.
 */
public interface FindUserPointPort {
    /**
     * @param userId 회원 식별자
     * @return 회원 포인트 존재 여부
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 식별자로 포인트 존재 여부를 확인합니다.
     */
    boolean existsByUserId(Long userId);

    /**
     * @param userKey 회원 키
     * @return 회원 포인트 존재 여부
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 키로 포인트 존재 여부를 확인합니다.
     */
    boolean existsByUserKey(String userKey);

    /**
     * @param userId 회원 식별자
     * @return 회원 포인트 {@link UserPoint}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 식별자로 포인트를 조회합니다.
     */
    Optional<UserPoint> findByUserId(Long userId);

    /**
     * @param userKey 회원 키
     * @return 회원 포인트 {@link UserPoint}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 회원 키로 포인트를 조회합니다.
     */
    Optional<UserPoint> findByUserKey(String userKey);
}
