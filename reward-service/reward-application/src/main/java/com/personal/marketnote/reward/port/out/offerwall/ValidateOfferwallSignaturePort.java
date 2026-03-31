package com.personal.marketnote.reward.port.out.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.domain.offerwall.UserDeviceType;

/**
 * 오퍼월 서명 검증 포트
 *
 * @Author 성효빈
 * @Date 2026-03-27
 * @Description 오퍼월 벤더별 서명 검증 기능을 제공합니다. 벤더별 구현 세부사항은 adapter에서 처리합니다.
 */
public interface ValidateOfferwallSignaturePort {
    /**
     * @param offerwallType  오퍼월 타입
     * @param userDeviceType 사용자 디바이스 타입
     * @param signedValue    서명 값
     * @param userKey        회원 키
     * @param rewardKey      리워드 키
     * @param quantity       수량
     * @param campaignKey    캠페인 키
     * @param rewardUnit     보상 화폐 단위
     * @Date 2026-03-27
     * @Author 성효빈
     * @Description 오퍼월 벤더별 서명을 검증합니다.
     */
    void validateSignature(
            OfferwallType offerwallType,
            UserDeviceType userDeviceType,
            String signedValue,
            String userKey,
            String rewardKey,
            Long quantity,
            String campaignKey,
            String rewardUnit
    );
}
