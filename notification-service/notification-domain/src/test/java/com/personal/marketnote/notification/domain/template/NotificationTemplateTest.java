package com.personal.marketnote.notification.domain.template;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationTemplateTest {

    @Nested
    @DisplayName("from(CreateState) 팩토리 메서드")
    class FromCreateState {

        @Test
        @DisplayName("유효한 값으로 알림 템플릿을 생성한다")
        void shouldCreateNotificationTemplateWithValidValues() {
            // given
            NotificationTemplateCreateState state = NotificationTemplateCreateState.builder()
                    .templateCode("ORDER_PAYMENT_COMPLETED")
                    .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                    .title("주문이 완료되었습니다")
                    .bodyTemplate("{productName} 외 {count}건이 결제되었습니다.")
                    .urlTemplate("/order/{orderId}")
                    .build();

            // when
            NotificationTemplate template = NotificationTemplate.from(state);

            // then
            assertThat(template.getTemplateCode()).isEqualTo("ORDER_PAYMENT_COMPLETED");
            assertThat(template.getNotificationType()).isEqualTo(NotificationType.ORDER_PAYMENT_COMPLETED);
            assertThat(template.getTitle()).isEqualTo("주문이 완료되었습니다");
            assertThat(template.getBodyTemplate()).isEqualTo("{productName} 외 {count}건이 결제되었습니다.");
            assertThat(template.getUrlTemplate()).isEqualTo("/order/{orderId}");
            assertThat(template.isActive()).isTrue();
        }

        @Test
        @DisplayName("templateCode가 null이면 예외가 발생한다")
        void shouldThrowExceptionWhenTemplateCodeIsNull() {
            // given
            NotificationTemplateCreateState state = NotificationTemplateCreateState.builder()
                    .templateCode(null)
                    .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                    .title("주문이 완료되었습니다")
                    .bodyTemplate("본문")
                    .urlTemplate("/order/{orderId}")
                    .build();

            // when & then
            assertThatThrownBy(() -> NotificationTemplate.from(state))
                    .isInstanceOf(InvalidTemplateCodeException.class);
        }

        @Test
        @DisplayName("templateCode가 빈 문자열이면 예외가 발생한다")
        void shouldThrowExceptionWhenTemplateCodeIsBlank() {
            // given
            NotificationTemplateCreateState state = NotificationTemplateCreateState.builder()
                    .templateCode("   ")
                    .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                    .title("주문이 완료되었습니다")
                    .bodyTemplate("본문")
                    .urlTemplate("/order/{orderId}")
                    .build();

            // when & then
            assertThatThrownBy(() -> NotificationTemplate.from(state))
                    .isInstanceOf(InvalidTemplateCodeException.class);
        }

        @Test
        @DisplayName("urlTemplate이 null이면 null로 생성된다")
        void shouldCreateTemplateWithNullUrlTemplate() {
            // given
            NotificationTemplateCreateState state = NotificationTemplateCreateState.builder()
                    .templateCode("POINT_ACCRUAL")
                    .notificationType(NotificationType.POINT_ACCRUAL)
                    .title("포인트가 적립되었습니다")
                    .bodyTemplate("{amount}P가 적립되었습니다.")
                    .urlTemplate(null)
                    .build();

            // when
            NotificationTemplate template = NotificationTemplate.from(state);

            // then
            assertThat(template.getUrlTemplate()).isNull();
        }
    }

    @Nested
    @DisplayName("from(SnapshotState) 팩토리 메서드")
    class FromSnapshotState {

        @Test
        @DisplayName("DB 스냅샷으로부터 알림 템플릿을 복원한다")
        void shouldRestoreNotificationTemplateFromSnapshot() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 12, 0);
            NotificationTemplateSnapshotState state = NotificationTemplateSnapshotState.builder()
                    .id(1L)
                    .templateCode("SHIPPING_STARTED")
                    .notificationType(NotificationType.SHIPPING_STARTED)
                    .title("배송이 시작되었습니다")
                    .bodyTemplate("{productName} 상품이 발송되었습니다.")
                    .urlTemplate("/order/{orderId}")
                    .status(EntityStatus.ACTIVE)
                    .createdAt(now)
                    .modifiedAt(now)
                    .build();

            // when
            NotificationTemplate template = NotificationTemplate.from(state);

            // then
            assertThat(template.getId()).isEqualTo(1L);
            assertThat(template.getTemplateCode()).isEqualTo("SHIPPING_STARTED");
            assertThat(template.getNotificationType()).isEqualTo(NotificationType.SHIPPING_STARTED);
            assertThat(template.getTitle()).isEqualTo("배송이 시작되었습니다");
            assertThat(template.getBodyTemplate()).isEqualTo("{productName} 상품이 발송되었습니다.");
            assertThat(template.getUrlTemplate()).isEqualTo("/order/{orderId}");
            assertThat(template.isActive()).isTrue();
            assertThat(template.getCreatedAt()).isEqualTo(now);
            assertThat(template.getModifiedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("비활성화 상태의 템플릿도 그대로 복원한다")
        void shouldRestoreInactiveTemplateFromSnapshot() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 12, 0);
            NotificationTemplateSnapshotState state = NotificationTemplateSnapshotState.builder()
                    .id(2L)
                    .templateCode("OLD_TEMPLATE")
                    .notificationType(NotificationType.NOTICE)
                    .title("공지사항")
                    .bodyTemplate("공지사항 본문")
                    .urlTemplate("/notice/{postId}")
                    .status(EntityStatus.INACTIVE)
                    .createdAt(now)
                    .modifiedAt(now)
                    .build();

            // when
            NotificationTemplate template = NotificationTemplate.from(state);

            // then
            assertThat(template.isInactive()).isTrue();
        }
    }

    @Nested
    @DisplayName("update 메서드")
    class Update {

        @Test
        @DisplayName("알림 템플릿 정보를 수정한다")
        void shouldUpdateNotificationTemplateInfo() {
            // given
            NotificationTemplate template = createDefaultTemplate();

            // when
            template.update("수정된 제목", "수정된 본문 {name}", "/updated/{id}");

            // then
            assertThat(template.getTitle()).isEqualTo("수정된 제목");
            assertThat(template.getBodyTemplate()).isEqualTo("수정된 본문 {name}");
            assertThat(template.getUrlTemplate()).isEqualTo("/updated/{id}");
        }

        @Test
        @DisplayName("urlTemplate을 null로 수정할 수 있다")
        void shouldUpdateUrlTemplateToNull() {
            // given
            NotificationTemplate template = createDefaultTemplate();

            // when
            template.update("제목", "본문", null);

            // then
            assertThat(template.getUrlTemplate()).isNull();
        }
    }

    @Nested
    @DisplayName("상태 전이 메서드")
    class StatusTransition {

        @Test
        @DisplayName("알림 템플릿을 비활성화한다")
        void shouldDeactivateNotificationTemplate() {
            // given
            NotificationTemplate template = createDefaultTemplate();
            assertThat(template.isActive()).isTrue();

            // when
            template.deactivate();

            // then
            assertThat(template.isInactive()).isTrue();
        }
    }

    private NotificationTemplate createDefaultTemplate() {
        NotificationTemplateCreateState state = NotificationTemplateCreateState.builder()
                .templateCode("ORDER_PAYMENT_COMPLETED")
                .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                .title("주문이 완료되었습니다")
                .bodyTemplate("{productName} 외 {count}건이 결제되었습니다.")
                .urlTemplate("/order/{orderId}")
                .build();
        return NotificationTemplate.from(state);
    }
}
