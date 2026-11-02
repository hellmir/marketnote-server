package com.personal.marketnote.notification.adapter.out.persistence.template;

import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.mapper.NotificationTemplateJpaEntityToDomainMapper;
import com.personal.marketnote.notification.adapter.out.persistence.template.entity.NotificationTemplateJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.template.repository.NotificationTemplateJpaRepository;
import com.personal.marketnote.notification.domain.template.DuplicateNotificationTemplateException;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationTemplateNotFoundException;
import com.personal.marketnote.notification.port.out.template.FindNotificationTemplatePort;
import com.personal.marketnote.notification.port.out.template.SaveNotificationTemplatePort;
import com.personal.marketnote.notification.port.out.template.UpdateNotificationTemplatePort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class NotificationTemplatePersistenceAdapter
        implements FindNotificationTemplatePort, SaveNotificationTemplatePort, UpdateNotificationTemplatePort {

    private final NotificationTemplateJpaRepository notificationTemplateJpaRepository;

    @Override
    public Optional<NotificationTemplate> findActiveById(Long id) {
        return notificationTemplateJpaRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .flatMap(NotificationTemplateJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public Optional<NotificationTemplate> findActiveByTemplateCode(String templateCode) {
        return notificationTemplateJpaRepository.findByTemplateCodeAndStatus(templateCode, EntityStatus.ACTIVE)
                .flatMap(NotificationTemplateJpaEntityToDomainMapper::mapToDomain);
    }

    @Override
    public List<NotificationTemplate> findAllActive() {
        return notificationTemplateJpaRepository.findAllByStatusOrderByCreatedAtDesc(EntityStatus.ACTIVE)
                .stream()
                .flatMap(entity -> NotificationTemplateJpaEntityToDomainMapper.mapToDomain(entity).stream())
                .toList();
    }

    @Override
    public Long save(NotificationTemplate notificationTemplate) {
        try {
            NotificationTemplateJpaEntity entity = NotificationTemplateJpaEntity.from(notificationTemplate);
            NotificationTemplateJpaEntity saved = notificationTemplateJpaRepository.save(entity);
            return saved.getId();
        } catch (DataIntegrityViolationException dive) {
            throw new DuplicateNotificationTemplateException(notificationTemplate.getTemplateCode());
        }
    }

    @Override
    public void update(NotificationTemplate notificationTemplate) {
        NotificationTemplateJpaEntity entity = notificationTemplateJpaRepository
                .findById(notificationTemplate.getId())
                .orElseThrow(() -> new NotificationTemplateNotFoundException(notificationTemplate.getId()));

        entity.updateFrom(notificationTemplate);
    }
}
