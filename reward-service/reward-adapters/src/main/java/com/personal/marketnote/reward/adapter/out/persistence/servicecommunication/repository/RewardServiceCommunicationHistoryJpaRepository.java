package com.personal.marketnote.reward.adapter.out.persistence.servicecommunication.repository;

import com.personal.marketnote.reward.adapter.out.persistence.servicecommunication.entity.RewardServiceCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardServiceCommunicationHistoryJpaRepository
        extends JpaRepository<RewardServiceCommunicationHistoryJpaEntity, Long> {
}
