package com.personal.marketnote.reward.service.offerwall;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.mapper.RewardCommandToStateMapper;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import com.personal.marketnote.reward.port.in.usecase.offerwall.GetOfferwallMapperUseCase;
import com.personal.marketnote.reward.port.in.usecase.offerwall.HandleOfferwallRewardUseCase;
import com.personal.marketnote.reward.port.in.usecase.offerwall.RegisterOfferwallRewardUseCase;
import com.personal.marketnote.reward.port.out.offerwall.SaveOfferwallMapperPort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class HandleOfferwallRewardService implements HandleOfferwallRewardUseCase {
    private final RegisterOfferwallRewardUseCase registerOfferwallRewardUseCase;
    private final GetOfferwallMapperUseCase getOfferwallMapperUseCase;
    private final SaveOfferwallMapperPort saveOfferwallMapperPort;

    @Override
    public Long handle(RegisterOfferwallRewardCommand command) {
        try {
            return registerOfferwallRewardUseCase.register(command);
        } catch (Exception e) {
            OfferwallMapper previouslyFailedMapper = getOfferwallMapperUseCase.findTopFailedOfferwallMapper(
                            command.offerwallType(), command.rewardKey()
                    )
                    .orElse(null);

            saveOfferwallMapperPort.save(
                    OfferwallMapper.fromFailed(
                            RewardCommandToStateMapper.mapToOfferwallMapperCreateState(command, false),
                            FormatValidator.hasValue(previouslyFailedMapper)
                                    ? previouslyFailedMapper.getFailureCount()
                                    : 0
                    )
            );

            throw e;
        }
    }
}
