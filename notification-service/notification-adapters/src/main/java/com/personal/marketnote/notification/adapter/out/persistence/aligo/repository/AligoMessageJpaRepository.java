package com.personal.marketnote.notification.adapter.out.persistence.aligo.repository;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageJpaEntity;
import com.personal.marketnote.notification.domain.aligo.AligoMessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AligoMessageJpaRepository extends JpaRepository<AligoMessageJpaEntity, Long> {

    @Query("""
            SELECT DISTINCT m FROM AligoMessageJpaEntity m
            LEFT JOIN FETCH m.recipients
            WHERE m.id = :id
            """)
    Optional<AligoMessageJpaEntity> findByIdWithRecipients(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT m FROM AligoMessageJpaEntity m
            LEFT JOIN FETCH m.recipients
            WHERE m.messageStatus = :status
            AND m.createdAt > :after
            ORDER BY m.createdAt ASC
            """)
    List<AligoMessageJpaEntity> findByMessageStatusAndCreatedAtAfter(
            @Param("status") AligoMessageStatus status,
            @Param("after") LocalDateTime after
    );

    @Query("""
            SELECT m FROM AligoMessageJpaEntity m
            WHERE m.createdAt >= :startDate
            AND m.createdAt <= :endDate
            ORDER BY m.createdAt DESC
            """)
    List<AligoMessageJpaEntity> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(m) FROM AligoMessageJpaEntity m
            WHERE m.createdAt >= :startDate
            AND m.createdAt <= :endDate
            """)
    long countByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
