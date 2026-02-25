package com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication;

import com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.entity.CommerceVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.repository.CommerceVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationHistory;
import com.personal.marketnote.commerce.port.out.vendorcommunication.SaveCommerceVendorCommunicationHistoryPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class CommerceVendorCommunicationHistoryPersistenceAdapter
        implements SaveCommerceVendorCommunicationHistoryPort {
    private final CommerceVendorCommunicationHistoryJpaRepository repository;

    @Override
    public CommerceVendorCommunicationHistory save(CommerceVendorCommunicationHistory history) {
        CommerceVendorCommunicationHistoryJpaEntity savedEntity = repository.save(
                CommerceVendorCommunicationHistoryJpaEntity.from(history)
        );
        return savedEntity.toDomain();
    }
}
