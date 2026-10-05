package com.personal.marketnote.notification.adapter.out.persistence.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.notification.entity.NotificationJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.notification.repository.NotificationJpaRepository;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.template.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPersistenceAdapterTest {

    @InjectMocks
    private NotificationPersistenceAdapter adapter;

    @Mock
    private NotificationJpaRepository notificationJpaRepository;

    @Nested
    @DisplayName("findActiveById")
    class FindActiveById {

        @Test
        @DisplayName("존재하면 도메인 객체를 반환한다")
        void returnsDomainWhenFound() {
            // given
            NotificationJpaEntity entity = createMockEntity(1L);
            when(notificationJpaRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<Notification> result = adapter.findActiveById(1L);

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("존재하지 않으면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(notificationJpaRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when
            Optional<Notification> result = adapter.findActiveById(1L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("사용자별 알림 목록을 커서 기반으로 반환한다")
        void returnsNotificationsWithCursor() {
            // given
            NotificationJpaEntity entity = createMockEntity(1L);
            when(notificationJpaRepository.findByUserIdWithCursor(
                    eq(100L), eq(EntityStatus.ACTIVE), eq(50L), any(PageRequest.class)
            )).thenReturn(List.of(entity));

            // when
            List<Notification> result = adapter.findByUserId(100L, 50L, 10);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("countUnreadByUserId")
    class CountUnreadByUserId {

        @Test
        @DisplayName("읽지 않은 알림 수를 반환한다")
        void returnsUnreadCount() {
            // given
            when(notificationJpaRepository.countByUserIdAndStatusAndIsRead(100L, EntityStatus.ACTIVE, false))
                    .thenReturn(5L);

            // when
            long count = adapter.countUnreadByUserId(100L);

            // then
            assertThat(count).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("알림을 저장하고 도메인 객체를 반환한다")
        void savesAndReturnsDomain() {
            // given
            Notification notification = createDomainNotification();
            NotificationJpaEntity savedEntity = createMockEntity(1L);
            when(notificationJpaRepository.save(any(NotificationJpaEntity.class))).thenReturn(savedEntity);

            // when
            Notification result = adapter.save(notification);

            // then
            assertThat(result).isNotNull();
            verify(notificationJpaRepository).save(any(NotificationJpaEntity.class));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("존재하는 알림을 업데이트한다")
        void updatesExistingNotification() {
            // given
            Notification notification = createDomainNotificationWithId(1L);
            NotificationJpaEntity entity = mock(NotificationJpaEntity.class);
            when(notificationJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            adapter.update(notification);

            // then
            verify(entity).updateFrom(notification);
        }

        @Test
        @DisplayName("존재하지 않으면 NotificationNotFoundException이 발생한다")
        void throwsExceptionWhenNotFound() {
            // given
            Notification notification = createDomainNotificationWithId(999L);
            when(notificationJpaRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adapter.update(notification))
                    .isInstanceOf(NotificationNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivateExpiredNotifications")
    class DeactivateExpiredNotifications {

        @Test
        @DisplayName("만료 알림을 비활성화하고 건수를 반환한다")
        void deactivatesAndReturnsCount() {
            // given
            LocalDateTime threshold = LocalDateTime.of(2026, 1, 1, 0, 0);
            when(notificationJpaRepository.deactivateExpiredNotifications(
                    threshold, EntityStatus.ACTIVE, EntityStatus.INACTIVE
            )).thenReturn(3);

            // when
            int count = adapter.deactivateExpiredNotifications(threshold);

            // then
            assertThat(count).isEqualTo(3);
        }
    }

    private NotificationJpaEntity createMockEntity(Long id) {
        NotificationJpaEntity entity = mock(NotificationJpaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getUserId()).thenReturn(100L);
        when(entity.getNotificationType()).thenReturn(NotificationType.ORDER_PAYMENT_COMPLETED);
        when(entity.getTitle()).thenReturn("주문 완료");
        when(entity.getBody()).thenReturn("주문이 완료되었습니다.");
        when(entity.getDeliveryChannel()).thenReturn(DeliveryChannel.PUSH_ONLY);
        when(entity.isRead()).thenReturn(false);
        when(entity.getSendStatus()).thenReturn(SendStatus.SENT);
        when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
        return entity;
    }

    private Notification createDomainNotification() {
        return Notification.from(
                NotificationCreateState.builder()
                        .userId(100L)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title("주문 완료")
                        .body("주문이 완료되었습니다.")
                        .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                        .build()
        );
    }

    private Notification createDomainNotificationWithId(Long id) {
        return Notification.from(
                NotificationSnapshotState.builder()
                        .id(id)
                        .userId(100L)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title("주문 완료")
                        .body("주문이 완료되었습니다.")
                        .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                        .isRead(false)
                        .sendStatus(SendStatus.SENT)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
