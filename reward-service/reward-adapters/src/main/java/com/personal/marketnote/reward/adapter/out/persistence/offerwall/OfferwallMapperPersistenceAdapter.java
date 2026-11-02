package com.personal.marketnote.reward.adapter.out.persistence.offerwall;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.offerwall.entity.OfferwallMapperJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.offerwall.repository.OfferwallMapperJpaRepository;
import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.offerwall.OfferwallType;
import com.personal.marketnote.reward.port.out.offerwall.FindOfferwallMapperPort;
import com.personal.marketnote.reward.port.out.offerwall.SaveOfferwallMapperPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class OfferwallMapperPersistenceAdapter implements SaveOfferwallMapperPort, FindOfferwallMapperPort {
    private final OfferwallMapperJpaRepository repository;

    @Override
    public OfferwallMapper save(OfferwallMapper offerwallMapper) {
        OfferwallMapperJpaEntity savedEntity = repository.save(OfferwallMapperJpaEntity.from(offerwallMapper));
        return savedEntity.toDomain();
    }

    @Override
    public boolean existsByOfferwallTypeAndRewardKeyAndIsSuccess(
            OfferwallType offerwallType, String rewardKey, boolean isSuccess
    ) {
        return repository.existsByOfferwallTypeAndRewardKeyAndIsSuccess(offerwallType, rewardKey, isSuccess);
    }

    @Override
    public Optional<OfferwallMapper> findTopFailedOfferwallMapper(OfferwallType offerwallType, String rewardKey) {
        return repository.findTop1ByOfferwallTypeAndRewardKeyAndIsSuccessFalseOrderByFailureCountDesc(
                        offerwallType, rewardKey
                )
                .map(OfferwallMapperJpaEntity::toDomain);
    }
}
