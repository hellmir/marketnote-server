package com.personal.marketnote.reward.port.in.usecase.vendorcommunication;

import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationHistory;
import com.personal.marketnote.reward.port.in.command.vendorcommunication.RewardVendorCommunicationHistoryCommand;

/**
 * 리워드 벤더 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 리워드 벤더 통신 기록 기능을 제공합니다.
 */
public interface RewardRecordVendorCommunicationHistoryUseCase {
    /**
     * @param command 리워드 벤더 통신 기록 커맨드
     * @return 리워드 벤더 통신 기록 {@link RewardVendorCommunicationHistory}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 리워드 벤더 통신 이력을 기록합니다.
     */
    RewardVendorCommunicationHistory record(RewardVendorCommunicationHistoryCommand command);
}
