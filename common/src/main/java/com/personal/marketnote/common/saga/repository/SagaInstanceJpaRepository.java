package com.personal.marketnote.common.saga.repository;

import com.personal.marketnote.common.saga.SagaStatus;
import com.personal.marketnote.common.saga.entity.SagaInstanceJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SagaInstanceJpaRepository extends JpaRepository<SagaInstanceJpaEntity, Long> {

    Optional<SagaInstanceJpaEntity> findBySagaId(String sagaId);

    List<SagaInstanceJpaEntity> findBySagaTypeAndStatus(String sagaType, SagaStatus status);

    @Query("SELECT s FROM SagaInstanceJpaEntity s WHERE s.status = :status AND s.modifiedAt < :cutoff ORDER BY s.modifiedAt ASC")
    List<SagaInstanceJpaEntity> findByStatusAndModifiedAtBefore(
            @Param("status") SagaStatus status,
            @Param("cutoff") LocalDateTime cutoff,
            Pageable pageable);
}
