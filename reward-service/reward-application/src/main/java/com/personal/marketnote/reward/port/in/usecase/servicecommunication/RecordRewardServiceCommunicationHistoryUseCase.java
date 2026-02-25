package com.personal.marketnote.reward.port.in.usecase.servicecommunication;

import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationHistory;
import com.personal.marketnote.reward.port.in.command.servicecommunication.RewardServiceCommunicationHistoryCommand;

/**
 * 리워드 서비스 간 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 리워드 서비스 간 통신 기록 기능을 제공합니다.
 */
public interface RecordRewardServiceCommunicationHistoryUseCase {
    /**
     * @param command 리워드 서비스 간 통신 기록 커맨드
     * @return 리워드 서비스 간 통신 기록 {@link RewardServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 리워드 서비스 간 통신 이력을 기록합니다.
     */
    RewardServiceCommunicationHistory record(RewardServiceCommunicationHistoryCommand command);
}
