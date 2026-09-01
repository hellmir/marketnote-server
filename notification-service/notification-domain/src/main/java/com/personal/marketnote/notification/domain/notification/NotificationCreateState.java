package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.notification.domain.template.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class NotificationCreateState {
    private Long userId;
    private NotificationType notificationType;
    private String title;
    private String body;
    private String data;
    private DeliveryChannel deliveryChannel;
    private String landingUrl;
    private LocalDateTime scheduledAt;
}
