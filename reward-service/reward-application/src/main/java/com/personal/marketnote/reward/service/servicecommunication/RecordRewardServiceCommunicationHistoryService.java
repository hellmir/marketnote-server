package com.personal.marketnote.reward.service.servicecommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationHistory;
import com.personal.marketnote.reward.mapper.RewardServiceCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.servicecommunication.RewardServiceCommunicationHistoryCommand;
import com.personal.marketnote.reward.port.in.usecase.servicecommunication.RecordRewardServiceCommunicationHistoryUseCase;
import com.personal.marketnote.reward.port.out.servicecommunication.SaveRewardServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, propagation = REQUIRES_NEW)
public class RecordRewardServiceCommunicationHistoryService
        implements RecordRewardServiceCommunicationHistoryUseCase {
    private final SaveRewardServiceCommunicationHistoryPort saveServiceCommunicationHistoryPort;

    @Override
    public RewardServiceCommunicationHistory record(RewardServiceCommunicationHistoryCommand command) {
        return saveServiceCommunicationHistoryPort.save(
                RewardServiceCommunicationHistory.from(
                        RewardServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
