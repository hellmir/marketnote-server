package com.personal.marketnote.user.adapter.out.persistence.servicecommunication.repository;

import com.personal.marketnote.user.adapter.out.persistence.servicecommunication.entity.UserServiceCommunicationHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserServiceCommunicationHistoryJpaRepository
        extends JpaRepository<UserServiceCommunicationHistoryJpaEntity, Long> {
}
