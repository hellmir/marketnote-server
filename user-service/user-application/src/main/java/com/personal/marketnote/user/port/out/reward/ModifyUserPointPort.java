package com.personal.marketnote.user.port.out.reward;

/**
 * 회원 포인트 변경 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 회원 포인트 변경 관련 기능을 제공합니다.
 */
public interface ModifyUserPointPort {
    /**
     * @param userId  회원 ID
     * @param userKey 회원 고유 키
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 회원 포인트 도메인을 생성합니다.
     */
    void registerUserPoint(Long userId, String userKey);

    /**
     * @param referrerUserId 추천한 회원 ID
     * @param referredUserId 추천받은 회원 ID
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 추천 포인트를 적립합니다.
     */
    void accrueReferralPoints(Long referrerUserId, Long referredUserId);
}
