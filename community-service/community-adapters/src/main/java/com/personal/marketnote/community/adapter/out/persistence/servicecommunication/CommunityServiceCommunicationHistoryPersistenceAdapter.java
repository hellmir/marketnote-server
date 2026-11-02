package com.personal.marketnote.community.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.community.adapter.out.persistence.servicecommunication.entity.CommunityServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.community.adapter.out.persistence.servicecommunication.repository.CommunityServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.community.domain.servicecommunication.CommunityServiceCommunicationHistory;
import com.personal.marketnote.community.port.out.servicecommunication.SaveCommunityServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class CommunityServiceCommunicationHistoryPersistenceAdapter
        implements SaveCommunityServiceCommunicationHistoryPort {
    private final CommunityServiceCommunicationHistoryJpaRepository repository;

    @Override
    public CommunityServiceCommunicationHistory save(CommunityServiceCommunicationHistory history) {
        CommunityServiceCommunicationHistoryJpaEntity savedEntity = repository.save(
                CommunityServiceCommunicationHistoryJpaEntity.from(history)
        );
        return savedEntity.toDomain();
    }
}
