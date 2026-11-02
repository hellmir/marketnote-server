package com.personal.marketnote.commerce.adapter.out.persistence.servicecommunication.repository;

import com.personal.marketnote.commerce.adapter.out.persistence.servicecommunication.entity.CommerceServiceCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommerceServiceCommunicationHistoryJpaRepository
        extends JpaRepository<CommerceServiceCommunicationHistoryJpaEntity, Long> {
}
