package com.personal.marketnote.reward.service.vendorcommunication;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationHistory;
import com.personal.marketnote.reward.mapper.RewardVendorCommunicationHistoryCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.vendorcommunication.RewardVendorCommunicationHistoryCommand;
import com.personal.marketnote.reward.port.in.usecase.vendorcommunication.RewardRecordVendorCommunicationHistoryUseCase;
import com.personal.marketnote.reward.port.out.vendorcommunication.RewardSaveVendorCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class RewardRecordVendorCommunicationHistoryService implements RewardRecordVendorCommunicationHistoryUseCase {
    private final RewardSaveVendorCommunicationHistoryPort saveVendorCommunicationHistoryPort;

    @Override
    public RewardVendorCommunicationHistory record(RewardVendorCommunicationHistoryCommand command) {
        return saveVendorCommunicationHistoryPort.save(
                RewardVendorCommunicationHistory.from(
                        RewardVendorCommunicationHistoryCommandToStateMapper.mapToCreateState(command)
                )
        );
    }
}
