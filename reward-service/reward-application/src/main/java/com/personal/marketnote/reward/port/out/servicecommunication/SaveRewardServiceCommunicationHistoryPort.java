package com.personal.marketnote.reward.port.out.servicecommunication;

import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationHistory;

/**
 * 리워드 서비스 간 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 리워드 서비스 간 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveRewardServiceCommunicationHistoryPort {
    /**
     * @param history 리워드 서비스 간 통신 기록
     * @return 저장된 리워드 서비스 간 통신 기록 {@link RewardServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 리워드 서비스 간 통신 기록을 저장합니다.
     */
    RewardServiceCommunicationHistory save(RewardServiceCommunicationHistory history);
}
