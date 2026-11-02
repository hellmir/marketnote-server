package com.personal.marketnote.notification.adapter.out.persistence.template.repository;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.template.entity.NotificationTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateJpaRepository extends JpaRepository<NotificationTemplateJpaEntity, Long> {

    Optional<NotificationTemplateJpaEntity> findByIdAndStatus(Long id, EntityStatus status);

    Optional<NotificationTemplateJpaEntity> findByTemplateCodeAndStatus(String templateCode, EntityStatus status);

    List<NotificationTemplateJpaEntity> findAllByStatusOrderByCreatedAtDesc(EntityStatus status);
}
