package com.personal.marketnote.fulfillment.adapter.out.persistence.servicecommunication;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.fulfillment.adapter.out.persistence.servicecommunication.entity.FulfillmentServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.servicecommunication.repository.FulfillmentServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistory;
import com.personal.marketnote.fulfillment.port.out.servicecommunication.SaveFulfillmentServiceCommunicationHistoryPort;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class FulfillmentServiceCommunicationHistoryPersistenceAdapter
        implements SaveFulfillmentServiceCommunicationHistoryPort {
    private final FulfillmentServiceCommunicationHistoryJpaRepository repository;

    @Override
    public FulfillmentServiceCommunicationHistory save(FulfillmentServiceCommunicationHistory history) {
        FulfillmentServiceCommunicationHistoryJpaEntity savedEntity = repository.save(
                FulfillmentServiceCommunicationHistoryJpaEntity.from(history)
        );
        return savedEntity.toDomain();
    }
}
