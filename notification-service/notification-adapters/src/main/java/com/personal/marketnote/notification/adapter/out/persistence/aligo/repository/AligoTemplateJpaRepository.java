package com.personal.marketnote.notification.adapter.out.persistence.aligo.repository;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AligoTemplateJpaRepository extends JpaRepository<AligoTemplateJpaEntity, Long> {

    @Query("""
            SELECT t FROM AligoTemplateJpaEntity t
            LEFT JOIN FETCH t.buttons
            WHERE t.templateCode = :templateCode AND t.status = 'ACTIVE'
            """)
    Optional<AligoTemplateJpaEntity> findByTemplateCodeWithButtons(@Param("templateCode") String templateCode);

    void deleteByTemplateCode(String templateCode);
}
