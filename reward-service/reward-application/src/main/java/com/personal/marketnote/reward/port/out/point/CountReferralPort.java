package com.personal.marketnote.reward.port.out.point;

/**
 * 친구 초대 누적 현황 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-03-21
 * @Description 친구 초대 완료 건수 및 누적 캐시를 조회합니다.
 */
public interface CountReferralPort {
    /**
     * @param userId 회원 식별자 (추천인)
     * @return 완료된 초대 건수
     * @Date 2026-03-21
     * @Author 성효빈
     * @Description 추천인의 초대 완료 건수를 조회합니다.
     */
    long countCompletedReferrals(Long userId);

    /**
     * @param userId 회원 식별자 (추천인)
     * @return 초대로 적립된 총 캐시 (기본 보상 + 누적 보너스)
     * @Date 2026-03-21
     * @Author 성효빈
     * @Description 추천인이 초대로 적립받은 총 캐시를 조회합니다.
     */
    long sumReferralEarnedAmount(Long userId);
}
