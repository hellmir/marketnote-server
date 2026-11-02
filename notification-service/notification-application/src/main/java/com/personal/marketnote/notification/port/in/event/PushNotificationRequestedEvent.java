package com.personal.marketnote.notification.port.in.event;

/**
 * 푸시 알림 발송 요청 이벤트.
 *
 * <p>{@code SendNotificationService}가 Notification 저장 트랜잭션을 커밋한 뒤,
 * {@code PushNotificationEventListener}가 수신하여 FCM 동기 호출을 수행하도록 분리하기 위해 사용된다.
 *
 * <p>트랜잭션 내부에서 FCM 동기 호출을 수행하면 DB 커넥션을 외부 HTTP 응답을 기다리는 동안 점유하게 되어
 * 커넥션 풀 고갈을 유발할 수 있다. AFTER_COMMIT 단계로 분리하여 트랜잭션 지연을 제거한다.
 */
public record PushNotificationRequestedEvent(
        Long notificationId,
        Long userId,
        String title,
        String body,
        String landingUrl
) {
}
