package com.personal.marketnote.fulfillment.adapter.out.persistence.servicecommunication.repository;

import com.personal.marketnote.fulfillment.adapter.out.persistence.servicecommunication.entity.FulfillmentServiceCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentServiceCommunicationHistoryJpaRepository
        extends JpaRepository<FulfillmentServiceCommunicationHistoryJpaEntity, Long> {
}
