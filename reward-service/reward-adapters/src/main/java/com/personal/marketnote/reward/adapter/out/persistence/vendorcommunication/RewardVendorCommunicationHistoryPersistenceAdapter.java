package com.personal.marketnote.reward.adapter.out.persistence.vendorcommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.reward.adapter.out.persistence.vendorcommunication.entity.RewardVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.vendorcommunication.repository.RewardVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationHistory;
import com.personal.marketnote.reward.port.out.vendorcommunication.RewardSaveVendorCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class RewardVendorCommunicationHistoryPersistenceAdapter implements RewardSaveVendorCommunicationHistoryPort {
    private final RewardVendorCommunicationHistoryJpaRepository repository;

    @Override
    public RewardVendorCommunicationHistory save(RewardVendorCommunicationHistory history) {
        RewardVendorCommunicationHistoryJpaEntity saved = repository.save(
                RewardVendorCommunicationHistoryJpaEntity.from(history)
        );
        return saved.toDomain();
    }
}
