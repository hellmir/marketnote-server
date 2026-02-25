package com.personal.marketnote.reward.port.out.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;

import java.util.Optional;

/**
 * 오퍼월 매퍼 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 오퍼월 매퍼 조회 관련 기능을 제공합니다.
 */
public interface FindOfferwallMapperPort {
    /**
     * @param offerwallType 오퍼월 유형
     * @param rewardKey     리워드 키
     * @param isSuccess     성공 여부
     * @return 오퍼월 매퍼 존재 여부
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 오퍼월 유형, 리워드 키, 성공 여부로 오퍼월 매퍼 존재 여부를 확인합니다.
     */
    boolean existsByOfferwallTypeAndRewardKeyAndIsSuccess(OfferwallType offerwallType, String rewardKey, boolean isSuccess);

    /**
     * @param offerwallType 오퍼월 유형
     * @param rewardKey     리워드 키
     * @return 실패한 오퍼월 매퍼 {@link OfferwallMapper}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 오퍼월 유형과 리워드 키로 가장 최근 실패한 오퍼월 매퍼를 조회합니다.
     */
    Optional<OfferwallMapper> findTopFailedOfferwallMapper(OfferwallType offerwallType, String rewardKey);
}
