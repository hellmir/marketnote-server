package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.template.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationTest {

    @Nested
    @DisplayName("from(CreateState) 팩토리 메서드")
    class FromCreateState {

        @Test
        @DisplayName("유효한 값으로 알림을 생성한다")
        void shouldCreateNotificationWithValidValues() {
            // given
            NotificationCreateState state = NotificationCreateState.builder()
                    .userId(1L)
                    .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                    .title("주문이 완료되었습니다")
                    .body("테스트 상품 외 1건이 결제되었습니다.")
                    .data("{\"orderId\":123}")
                    .deliveryChannel(DeliveryChannel.PUSH_AND_IN_APP)
                    .landingUrl("/order/123")
                    .build();

            // when
            Notification notification = Notification.from(state);

            // then
            assertThat(notification.getUserId()).isEqualTo(1L);
            assertThat(notification.getNotificationType()).isEqualTo(NotificationType.ORDER_PAYMENT_COMPLETED);
            assertThat(notification.getTitle()).isEqualTo("주문이 완료되었습니다");
            assertThat(notification.getBody()).isEqualTo("테스트 상품 외 1건이 결제되었습니다.");
            assertThat(notification.getData()).isEqualTo("{\"orderId\":123}");
            assertThat(notification.getDeliveryChannel()).isEqualTo(DeliveryChannel.PUSH_AND_IN_APP);
            assertThat(notification.isRead()).isFalse();
            assertThat(notification.getLandingUrl()).isEqualTo("/order/123");
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.PENDING);
            assertThat(notification.isActive()).isTrue();
        }

        @Test
        @DisplayName("scheduledAt이 존재하면 sendStatus가 SCHEDULED로 설정된다")
        void shouldSetScheduledStatusWhenScheduledAtExists() {
            // given
            LocalDateTime scheduledAt = LocalDateTime.of(2026, 4, 10, 9, 0);
            NotificationCreateState state = NotificationCreateState.builder()
                    .userId(1L)
                    .notificationType(NotificationType.PURCHASE_CONFIRMATION_REQUEST)
                    .title("구매 확정 요청")
                    .body("구매 확정을 해주세요.")
                    .deliveryChannel(DeliveryChannel.PUSH_AND_IN_APP)
                    .scheduledAt(scheduledAt)
                    .build();

            // when
            Notification notification = Notification.from(state);

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.SCHEDULED);
            assertThat(notification.getScheduledAt()).isEqualTo(scheduledAt);
        }

        @Test
        @DisplayName("userId가 null이면 예외가 발생한다")
        void shouldThrowExceptionWhenUserIdIsNull() {
            // given
            NotificationCreateState state = NotificationCreateState.builder()
                    .userId(null)
                    .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                    .title("제목")
                    .body("본문")
                    .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                    .build();

            // when & then
            assertThatThrownBy(() -> Notification.from(state))
                    .isInstanceOf(InvalidNotificationException.class);
        }

        @Test
        @DisplayName("notificationType이 null이면 예외가 발생한다")
        void shouldThrowExceptionWhenNotificationTypeIsNull() {
            // given
            NotificationCreateState state = NotificationCreateState.builder()
                    .userId(1L)
                    .notificationType(null)
                    .title("제목")
                    .body("본문")
                    .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                    .build();

            // when & then
            assertThatThrownBy(() -> Notification.from(state))
                    .isInstanceOf(InvalidNotificationException.class);
        }

        @Test
        @DisplayName("deliveryChannel이 null이면 예외가 발생한다")
        void shouldThrowExceptionWhenDeliveryChannelIsNull() {
            // given
            NotificationCreateState state = NotificationCreateState.builder()
                    .userId(1L)
                    .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                    .title("제목")
                    .body("본문")
                    .deliveryChannel(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> Notification.from(state))
                    .isInstanceOf(InvalidNotificationException.class);
        }
    }

    @Nested
    @DisplayName("from(SnapshotState) 팩토리 메서드")
    class FromSnapshotState {

        @Test
        @DisplayName("DB 스냅샷으로부터 알림을 복원한다")
        void shouldRestoreNotificationFromSnapshot() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 12, 0);
            NotificationSnapshotState state = NotificationSnapshotState.builder()
                    .id(10L)
                    .userId(1L)
                    .notificationType(NotificationType.SHIPPING_STARTED)
                    .title("배송이 시작되었습니다")
                    .body("테스트 상품이 발송되었습니다.")
                    .data("{\"orderId\":456}")
                    .deliveryChannel(DeliveryChannel.PUSH_AND_IN_APP)
                    .isRead(true)
                    .landingUrl("/order/456")
                    .sendStatus(SendStatus.SENT)
                    .scheduledAt(null)
                    .status(EntityStatus.ACTIVE)
                    .createdAt(now)
                    .modifiedAt(now)
                    .build();

            // when
            Notification notification = Notification.from(state);

            // then
            assertThat(notification.getId()).isEqualTo(10L);
            assertThat(notification.getUserId()).isEqualTo(1L);
            assertThat(notification.getNotificationType()).isEqualTo(NotificationType.SHIPPING_STARTED);
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.SENT);
            assertThat(notification.isActive()).isTrue();
            assertThat(notification.getCreatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("비활성화 상태의 알림도 그대로 복원한다")
        void shouldRestoreInactiveNotificationFromSnapshot() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 12, 0);
            NotificationSnapshotState state = NotificationSnapshotState.builder()
                    .id(20L)
                    .userId(1L)
                    .notificationType(NotificationType.NOTICE)
                    .title("공지사항")
                    .body("본문")
                    .deliveryChannel(DeliveryChannel.IN_APP_ONLY)
                    .isRead(false)
                    .sendStatus(SendStatus.SENT)
                    .status(EntityStatus.INACTIVE)
                    .createdAt(now)
                    .modifiedAt(now)
                    .build();

            // when
            Notification notification = Notification.from(state);

            // then
            assertThat(notification.isInactive()).isTrue();
        }
    }

    @Nested
    @DisplayName("상태 전이 메서드")
    class StatusTransition {

        @Test
        @DisplayName("읽음 처리를 한다")
        void shouldMarkAsRead() {
            // given
            Notification notification = createDefaultNotification();
            assertThat(notification.isRead()).isFalse();

            // when
            notification.markAsRead();

            // then
            assertThat(notification.isRead()).isTrue();
        }

        @Test
        @DisplayName("발송 완료로 전환한다")
        void shouldMarkAsSent() {
            // given
            Notification notification = createDefaultNotification();
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.PENDING);

            // when
            notification.markAsSent();

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.SENT);
        }

        @Test
        @DisplayName("발송 실패로 전환한다")
        void shouldMarkAsFailed() {
            // given
            Notification notification = createDefaultNotification();

            // when
            notification.markAsFailed();

            // then
            assertThat(notification.getSendStatus()).isEqualTo(SendStatus.FAILED);
        }
    }

    private Notification createDefaultNotification() {
        NotificationCreateState state = NotificationCreateState.builder()
                .userId(1L)
                .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                .title("주문이 완료되었습니다")
                .body("테스트 상품 외 1건이 결제되었습니다.")
                .deliveryChannel(DeliveryChannel.PUSH_AND_IN_APP)
                .landingUrl("/order/123")
                .build();
        return Notification.from(state);
    }
}
