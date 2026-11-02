package com.personal.marketnote.community.adapter.out.persistence.servicecommunication.repository;

import com.personal.marketnote.community.adapter.out.persistence.servicecommunication.entity.CommunityServiceCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityServiceCommunicationHistoryJpaRepository
        extends JpaRepository<CommunityServiceCommunicationHistoryJpaEntity, Long> {
}
