package com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.entity.CommerceVendorCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommerceVendorCommunicationHistoryJpaRepository
        extends JpaRepository<CommerceVendorCommunicationHistoryJpaEntity, Long> {
}
