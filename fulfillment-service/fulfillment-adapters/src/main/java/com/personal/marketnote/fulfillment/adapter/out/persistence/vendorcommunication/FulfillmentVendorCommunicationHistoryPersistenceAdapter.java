package com.personal.marketnote.fulfillment.adapter.out.persistence.vendorcommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.fulfillment.adapter.out.persistence.vendorcommunication.entity.FulfillmentVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.vendorcommunication.repository.FulfillmentVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistory;
import com.personal.marketnote.fulfillment.port.out.vendorcommunication.SaveFulfillmentVendorCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class FulfillmentVendorCommunicationHistoryPersistenceAdapter
        implements SaveFulfillmentVendorCommunicationHistoryPort {
    private final FulfillmentVendorCommunicationHistoryJpaRepository repository;

    @Override
    public FulfillmentVendorCommunicationHistory save(FulfillmentVendorCommunicationHistory history) {
        FulfillmentVendorCommunicationHistoryJpaEntity savedEntity = repository.save(
                FulfillmentVendorCommunicationHistoryJpaEntity.from(history)
        );
        return savedEntity.toDomain();
    }
}
