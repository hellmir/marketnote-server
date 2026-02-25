package com.personal.marketnote.reward.port.out.vendorcommunication;

import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationHistory;

/**
 * 리워드 벤더 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 리워드 벤더 통신 기록 저장 기능을 제공합니다.
 */
public interface RewardSaveVendorCommunicationHistoryPort {
    /**
     * @param history 리워드 벤더 통신 기록
     * @return 저장된 리워드 벤더 통신 기록 {@link RewardVendorCommunicationHistory}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 리워드 벤더 통신 기록을 저장합니다.
     */
    RewardVendorCommunicationHistory save(RewardVendorCommunicationHistory history);
}
