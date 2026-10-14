package com.personal.marketnote.notification.adapter.out.persistence.aligo.repository;

import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AligoBrandTalkTemplateJpaRepository extends JpaRepository<AligoBrandTalkTemplateJpaEntity, Long> {

    Optional<AligoBrandTalkTemplateJpaEntity> findByTemplateCode(String templateCode);
}
