package com.personal.marketnote.common.outbox.repository;

import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

    List<OutboxEventJpaEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    void deleteByStatusAndPublishedAtBefore(OutboxEventStatus status, LocalDateTime before);
}
