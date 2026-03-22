package com.personal.marketnote.reward.port.in.usecase.point;

import com.personal.marketnote.reward.port.in.result.point.GetReferralStatusResult;

/**
 * 친구 초대 현황 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-21
 * @Description 친구 초대 현황 조회 및 누적 초대 건수 관련 기능을 제공합니다.
 */
public interface GetReferralStatusUseCase {
    /**
     * @param userId 회원 식별자 (추천인)
     * @return 친구 초대 현황 {@link GetReferralStatusResult}
     * @Date 2026-03-21
     * @Author 성효빈
     * @Description 추천인의 초대 현황(초대 수, 총 캐시, 보너스 달성 현황)을 조회합니다.
     */
    GetReferralStatusResult getReferralStatus(Long userId);

    /**
     * @param userId 회원 식별자 (추천인)
     * @return 완료된 초대 건수
     * @Date 2026-03-21
     * @Author 성효빈
     * @Description 추천인의 완료된 초대 건수를 조회합니다.
     */
    long countCompletedReferrals(Long userId);
}
