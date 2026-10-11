package com.personal.marketnote.notification.adapter.out.persistence.aligo.repository;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AligoMessageJpaRepository extends JpaRepository<AligoMessageJpaEntity, Long> {

    @Query("""
            SELECT DISTINCT m FROM AligoMessageJpaEntity m
            LEFT JOIN FETCH m.recipients
            WHERE m.id = :id
            """)
    Optional<AligoMessageJpaEntity> findByIdWithRecipients(@Param("id") Long id);
}
