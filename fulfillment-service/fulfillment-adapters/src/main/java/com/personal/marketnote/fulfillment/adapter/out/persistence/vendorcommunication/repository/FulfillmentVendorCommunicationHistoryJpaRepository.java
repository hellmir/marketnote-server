package com.personal.marketnote.fulfillment.adapter.out.persistence.vendorcommunication.repository;

import com.personal.marketnote.fulfillment.adapter.out.persistence.vendorcommunication.entity.FulfillmentVendorCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FulfillmentVendorCommunicationHistoryJpaRepository
        extends JpaRepository<FulfillmentVendorCommunicationHistoryJpaEntity, Long> {
}
