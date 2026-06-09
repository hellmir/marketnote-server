package com.personal.marketnote.notification.adapter.out.persistence.template.entity;

import com.personal.marketnote.common.adapter.out.persistence.audit.BaseGeneralEntity;
import com.personal.marketnote.notification.domain.template.NotificationTemplate;
import com.personal.marketnote.notification.domain.template.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "notification_template", indexes = {
        @Index(name = "idx_notification_template_template_code", columnList = "template_code", unique = true)
})
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class NotificationTemplateJpaEntity extends BaseGeneralEntity {

    @Column(name = "template_code", nullable = false, unique = true, length = 100)
    private String templateCode;

    @Column(name = "notification_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body_template", nullable = false, length = 500)
    private String bodyTemplate;

    @Column(name = "url_template", length = 500)
    private String urlTemplate;

    public static NotificationTemplateJpaEntity from(NotificationTemplate template) {
        return NotificationTemplateJpaEntity.builder()
                .templateCode(template.getTemplateCode())
                .notificationType(template.getNotificationType())
                .title(template.getTitle())
                .bodyTemplate(template.getBodyTemplate())
                .urlTemplate(template.getUrlTemplate())
                .build();
    }

    public void updateFrom(NotificationTemplate template) {
        if (template.isInactive()) {
            deactivate();
        }
        this.title = template.getTitle();
        this.bodyTemplate = template.getBodyTemplate();
        this.urlTemplate = template.getUrlTemplate();
    }
}
