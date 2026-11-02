package com.personal.marketnote.reward.service.offerwall;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.port.in.usecase.offerwall.GetOfferwallMapperUseCase;
import com.personal.marketnote.reward.port.out.offerwall.FindOfferwallMapperPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@UseCase
@RequiredArgsConstructor
public class GetOfferwallMapperService implements GetOfferwallMapperUseCase {
    private final FindOfferwallMapperPort findOfferwallMapperPort;

    public boolean existsSucceededOfferwallMapper(OfferwallType offerwallType, String rewardKey) {
        return findOfferwallMapperPort.existsByOfferwallTypeAndRewardKeyAndIsSuccess(offerwallType, rewardKey, true);
    }

    public Optional<OfferwallMapper> findTopFailedOfferwallMapper(OfferwallType offerwallType, String rewardKey) {
        return findOfferwallMapperPort.findTopFailedOfferwallMapper(offerwallType, rewardKey);
    }
}
