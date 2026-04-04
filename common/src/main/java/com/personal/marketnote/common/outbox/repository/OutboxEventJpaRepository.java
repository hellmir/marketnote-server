package com.personal.marketnote.common.outbox.repository;

import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, Long> {

    List<OutboxEventJpaEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    void deleteByStatusAndPublishedAtBefore(OutboxEventStatus status, LocalDateTime before);

    List<OutboxEventJpaEntity> findByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    List<OutboxEventJpaEntity> findByStatusAndTopicOrderByCreatedAtAsc(OutboxEventStatus status, String topic);

    long countByStatus(OutboxEventStatus status);

    @Query("SELECT DISTINCT e.topic FROM OutboxEventJpaEntity e WHERE e.status = :status")
    List<String> findDistinctTopicByStatus(@Param("status") OutboxEventStatus status);

    void deleteByStatusAndDiscardedAtBefore(OutboxEventStatus status, LocalDateTime before);
}
