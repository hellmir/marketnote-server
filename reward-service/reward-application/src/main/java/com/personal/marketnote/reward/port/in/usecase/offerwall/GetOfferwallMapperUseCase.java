package com.personal.marketnote.reward.port.in.usecase.offerwall;

import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;

import java.util.Optional;

/**
 * 오퍼월 매퍼 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-20
 * @Description 오퍼월 매퍼 조회 관련 기능을 제공합니다.
 */
public interface GetOfferwallMapperUseCase {
    /**
     * @param offerwallType 오퍼월 타입
     * @param rewardKey     리워드 키
     * @return 성공한 오퍼월 리워드 지급 정보 존재 여부 {@link boolean}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 성공한 오퍼월 리워드 지급 정보 존재 여부를 조회합니다.
     */
    boolean existsSucceededOfferwallMapper(OfferwallType offerwallType, String rewardKey);

    /**
     * @param offerwallType 오퍼월 타입
     * @param rewardKey     리워드 키
     * @return 실패한 오퍼월 리워드 지급 정보 중 가장 높은 failureCount를 가진 정보 {@link OfferwallMapper}
     * @Date 2026-01-20
     * @Author 성효빈
     * @Description 실패한 오퍼월 리워드 지급 정보 중 failureCount가 가장 높은 정보를 조회합니다.
     */
    Optional<OfferwallMapper> findTopFailedOfferwallMapper(OfferwallType offerwallType, String rewardKey);
}
