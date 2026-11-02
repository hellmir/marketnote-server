package com.personal.marketnote.reward.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.servicecommunication.entity.RewardServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.servicecommunication.repository.RewardServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.reward.domain.servicecommunication.RewardServiceCommunicationHistory;
import com.personal.marketnote.reward.port.out.servicecommunication.SaveRewardServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class RewardServiceCommunicationHistoryPersistenceAdapter
        implements SaveRewardServiceCommunicationHistoryPort {
    private final RewardServiceCommunicationHistoryJpaRepository repository;

    @Override
    public RewardServiceCommunicationHistory save(RewardServiceCommunicationHistory history) {
        RewardServiceCommunicationHistoryJpaEntity savedEntity = repository.save(
                RewardServiceCommunicationHistoryJpaEntity.from(history)
        );
        return savedEntity.toDomain();
    }
}
