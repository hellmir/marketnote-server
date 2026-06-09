package com.personal.marketnote.notification.domain.template;

import com.personal.marketnote.common.domain.BaseDomain;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class NotificationTemplate extends BaseDomain {
    private Long id;
    private String templateCode;
    private NotificationType notificationType;
    private String title;
    private String bodyTemplate;
    private String urlTemplate;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static NotificationTemplate from(NotificationTemplateCreateState state) {
        validateTemplateCode(state.getTemplateCode());

        NotificationTemplate template = NotificationTemplate.builder()
                .templateCode(state.getTemplateCode())
                .notificationType(state.getNotificationType())
                .title(state.getTitle())
                .bodyTemplate(state.getBodyTemplate())
                .urlTemplate(state.getUrlTemplate())
                .build();
        template.activate();
        return template;
    }

    public static NotificationTemplate from(NotificationTemplateSnapshotState state) {
        NotificationTemplate template = NotificationTemplate.builder()
                .id(state.getId())
                .templateCode(state.getTemplateCode())
                .notificationType(state.getNotificationType())
                .title(state.getTitle())
                .bodyTemplate(state.getBodyTemplate())
                .urlTemplate(state.getUrlTemplate())
                .createdAt(state.getCreatedAt())
                .modifiedAt(state.getModifiedAt())
                .build();
        template.status = state.getStatus();
        return template;
    }

    public void update(String title, String bodyTemplate, String urlTemplate) {
        this.title = title;
        this.bodyTemplate = bodyTemplate;
        this.urlTemplate = urlTemplate;
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    private static void validateTemplateCode(String templateCode) {
        if (FormatValidator.hasNoValue(templateCode)) {
            throw new InvalidTemplateCodeException("템플릿 코드는 필수입니다.");
        }
    }
}
