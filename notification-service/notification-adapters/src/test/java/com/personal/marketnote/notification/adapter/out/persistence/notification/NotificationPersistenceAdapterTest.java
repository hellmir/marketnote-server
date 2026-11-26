package com.personal.marketnote.notification.adapter.out.persistence.notification;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.notification.entity.NotificationJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.notification.repository.NotificationJpaRepository;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.template.NotificationDisplayCategory;
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
            List<Notification> result = adapter.findByUserId(100L, 50L, 10, null);

            // then
            assertThat(result).hasSize(1);
            verify(notificationJpaRepository).findByUserIdWithCursor(
                    eq(100L), eq(EntityStatus.ACTIVE), eq(50L), any(PageRequest.class)
            );
        }

        @Test
        @DisplayName("카테고리 전달 시 카테고리 전용 쿼리를 호출한다")
        void callsTypesQueryWhenCategoryGiven() {
            // given
            NotificationJpaEntity entity = createMockEntity(1L);
            NotificationDisplayCategory category = NotificationDisplayCategory.ORDER_SHIPPING;
            when(notificationJpaRepository.findByUserIdWithCursorAndTypes(
                    eq(100L), eq(EntityStatus.ACTIVE), eq(50L), eq(category.types()), any(PageRequest.class)
            )).thenReturn(List.of(entity));

            // when
            List<Notification> result = adapter.findByUserId(100L, 50L, 10, category);

            // then
            assertThat(result).hasSize(1);
            verify(notificationJpaRepository).findByUserIdWithCursorAndTypes(
                    eq(100L), eq(EntityStatus.ACTIVE), eq(50L), eq(category.types()), any(PageRequest.class)
            );
        }

        @Test
        @DisplayName("null 커서 전달 시 -1L로 치환하여 쿼리를 호출한다")
        void translatesNullCursorToMinusOne() {
            // given
            when(notificationJpaRepository.findByUserIdWithCursor(
                    eq(100L), eq(EntityStatus.ACTIVE), eq(-1L), any(PageRequest.class)
            )).thenReturn(List.of());

            // when
            adapter.findByUserId(100L, null, 10, null);

            // then
            verify(notificationJpaRepository).findByUserIdWithCursor(
                    eq(100L), eq(EntityStatus.ACTIVE), eq(-1L), any(PageRequest.class)
            );
        }
    }

    @Nested
    @DisplayName("countByUserId")
    class CountByUserId {

        @Test
        @DisplayName("카테고리 미전달 시 전체 카운트 쿼리를 호출한다")
        void callsFullCountWhenNoCategory() {
            // given
            when(notificationJpaRepository.countByUserIdAndStatus(100L, EntityStatus.ACTIVE)).thenReturn(9L);

            // when
            long count = adapter.countByUserId(100L, null);

            // then
            assertThat(count).isEqualTo(9L);
            verify(notificationJpaRepository).countByUserIdAndStatus(100L, EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("카테고리 전달 시 카테고리 전용 카운트 쿼리를 호출한다")
        void callsTypesCountWhenCategoryGiven() {
            // given
            NotificationDisplayCategory category = NotificationDisplayCategory.NOTICE_EVENT;
            when(notificationJpaRepository.countByUserIdAndStatusAndTypes(
                    100L, EntityStatus.ACTIVE, category.types()
            )).thenReturn(3L);

            // when
            long count = adapter.countByUserId(100L, category);

            // then
            assertThat(count).isEqualTo(3L);
            verify(notificationJpaRepository).countByUserIdAndStatusAndTypes(
                    100L, EntityStatus.ACTIVE, category.types()
            );
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
    @DisplayName("markAllAsReadByUserId")
    class MarkAllAsReadByUserId {

        @Test
        @DisplayName("JpaRepository markAllAsReadByUserIdAndStatus 호출 결과를 그대로 반환한다")
        void delegatesToRepositoryAndReturnsCount() {
            // given
            when(notificationJpaRepository.markAllAsReadByUserIdAndStatus(100L, EntityStatus.ACTIVE))
                    .thenReturn(4);

            // when
            int updated = adapter.markAllAsReadByUserId(100L);

            // then
            assertThat(updated).isEqualTo(4);
            verify(notificationJpaRepository).markAllAsReadByUserIdAndStatus(100L, EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("미읽음이 없으면 0을 반환한다")
        void returnsZeroWhenNoUnread() {
            // given
            when(notificationJpaRepository.markAllAsReadByUserIdAndStatus(100L, EntityStatus.ACTIVE))
                    .thenReturn(0);

            // when
            int updated = adapter.markAllAsReadByUserId(100L);

            // then
            assertThat(updated).isEqualTo(0);
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

    @Nested
    @DisplayName("findScheduledNotificationsDue")
    class FindScheduledNotificationsDue {

        @Test
        @DisplayName("SCHEDULED 상태이며 발송 시각이 도래한 알림 목록을 반환한다")
        void returnsScheduledNotifications() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 28, 12, 0);
            NotificationJpaEntity entity = createMockEntity(1L);
            when(notificationJpaRepository.findScheduledNotificationsDue(
                    SendStatus.SCHEDULED, now, EntityStatus.ACTIVE
            )).thenReturn(List.of(entity));

            // when
            List<Notification> result = adapter.findScheduledNotificationsDue(now);

            // then
            assertThat(result).hasSize(1);
            verify(notificationJpaRepository).findScheduledNotificationsDue(
                    SendStatus.SCHEDULED, now, EntityStatus.ACTIVE
            );
        }

        @Test
        @DisplayName("SCHEDULED 상태 알림이 없으면 빈 리스트를 반환한다")
        void returnsEmptyWhenNoScheduledNotifications() {
            // given
            LocalDateTime now = LocalDateTime.of(2026, 4, 28, 12, 0);
            when(notificationJpaRepository.findScheduledNotificationsDue(
                    SendStatus.SCHEDULED, now, EntityStatus.ACTIVE
            )).thenReturn(List.of());

            // when
            List<Notification> result = adapter.findScheduledNotificationsDue(now);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("saveAll")
    class SaveAll {

        @Test
        @DisplayName("알림 목록을 일괄 저장하고 도메인 객체 목록을 반환한다")
        @SuppressWarnings("unchecked")
        void savesAllAndReturnsDomains() {
            // given
            List<Notification> notifications = List.of(
                    createDomainNotification(),
                    createDomainNotification()
            );
            List<NotificationJpaEntity> savedEntities = List.of(
                    createMockEntity(1L), createMockEntity(2L)
            );
            when(notificationJpaRepository.saveAll(any(List.class))).thenReturn(savedEntities);

            // when
            List<Notification> result = adapter.saveAll(notifications);

            // then
            assertThat(result).hasSize(2);
            verify(notificationJpaRepository).saveAll(any(List.class));
        }
    }

    @Nested
    @DisplayName("updateAll")
    class UpdateAll {

        @Test
        @DisplayName("ID 와 매칭되는 엔티티의 updateFrom 을 호출한다")
        void updatesMatchingEntities() {
            // given
            Notification notification1 = createDomainNotificationWithId(1L);
            Notification notification2 = createDomainNotificationWithId(2L);
            NotificationJpaEntity entity1 = mock(NotificationJpaEntity.class);
            NotificationJpaEntity entity2 = mock(NotificationJpaEntity.class);
            when(entity1.getId()).thenReturn(1L);
            when(entity2.getId()).thenReturn(2L);
            when(notificationJpaRepository.findAllById(List.of(1L, 2L)))
                    .thenReturn(List.of(entity1, entity2));

            // when
            adapter.updateAll(List.of(notification1, notification2));

            // then
            verify(entity1).updateFrom(notification1);
            verify(entity2).updateFrom(notification2);
        }

        @Test
        @DisplayName("매칭되는 엔티티가 없는 알림은 무시한다")
        void skipsNonMatchingNotifications() {
            // given
            Notification notification1 = createDomainNotificationWithId(1L);
            Notification notification2 = createDomainNotificationWithId(999L);
            NotificationJpaEntity entity1 = mock(NotificationJpaEntity.class);
            when(entity1.getId()).thenReturn(1L);
            when(notificationJpaRepository.findAllById(List.of(1L, 999L)))
                    .thenReturn(List.of(entity1));

            // when
            adapter.updateAll(List.of(notification1, notification2));

            // then
            verify(entity1).updateFrom(notification1);
            verify(entity1, never()).updateFrom(notification2);
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
