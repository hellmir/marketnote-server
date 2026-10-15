package com.personal.marketnote.notification.adapter.out.persistence.aligo.repository;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkMessageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AligoBrandTalkMessageJpaRepository extends JpaRepository<AligoBrandTalkMessageJpaEntity, Long> {

    @Query("""
            SELECT m FROM AligoBrandTalkMessageJpaEntity m
            LEFT JOIN FETCH m.recipients
            WHERE m.id = :id
            """)
    Optional<AligoBrandTalkMessageJpaEntity> findByIdWithRecipients(@Param("id") Long id);
}
