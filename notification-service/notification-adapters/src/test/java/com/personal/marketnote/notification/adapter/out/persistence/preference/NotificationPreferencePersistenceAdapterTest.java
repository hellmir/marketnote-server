package com.personal.marketnote.notification.adapter.out.persistence.preference;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.preference.repository.NotificationPreferenceJpaRepository;
import com.personal.marketnote.notification.domain.preference.*;
import com.personal.marketnote.notification.domain.template.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationPreferencePersistenceAdapterTest {

    @InjectMocks
    private NotificationPreferencePersistenceAdapter adapter;

    @Mock
    private NotificationPreferenceJpaRepository notificationPreferenceJpaRepository;

    @Nested
    @DisplayName("saveAll")
    class SaveAll {

        @Test
        @DisplayName("수신 설정 목록을 일괄 저장한다")
        void savesAllPreferences() {
            // given
            NotificationPreference preference = createDomainPreference(100L);

            // when
            adapter.saveAll(List.of(preference));

            // then
            verify(notificationPreferenceJpaRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("중복이면 DuplicateNotificationPreferenceException이 발생한다")
        void throwsExceptionOnDuplicate() {
            // given
            NotificationPreference preference = createDomainPreference(100L);
            when(notificationPreferenceJpaRepository.saveAll(anyList()))
                    .thenThrow(new DataIntegrityViolationException("duplicate"));

            // when & then
            assertThatThrownBy(() -> adapter.saveAll(List.of(preference)))
                    .isInstanceOf(DuplicateNotificationPreferenceException.class);
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("사용자별 수신 설정 목록을 반환한다")
        void returnsPreferencesByUserId() {
            // given
            NotificationPreferenceJpaEntity entity = createMockEntity(1L, 100L);
            when(notificationPreferenceJpaRepository.findAllByUserIdAndStatus(100L, EntityStatus.ACTIVE))
                    .thenReturn(List.of(entity));

            // when
            List<NotificationPreference> result = adapter.findAllByUserId(100L);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findByUserIdAndNotificationType")
    class FindByUserIdAndNotificationType {

        @Test
        @DisplayName("사용자와 알림 유형으로 수신 설정을 반환한다")
        void returnsPreference() {
            // given
            NotificationPreferenceJpaEntity entity = createMockEntity(1L, 100L);
            when(notificationPreferenceJpaRepository.findByUserIdAndNotificationTypeAndStatus(
                    100L, NotificationType.ORDER_PAYMENT_COMPLETED, EntityStatus.ACTIVE
            )).thenReturn(Optional.of(entity));

            // when
            Optional<NotificationPreference> result = adapter.findByUserIdAndNotificationType(
                    100L, NotificationType.ORDER_PAYMENT_COMPLETED
            );

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("존재하지 않으면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(notificationPreferenceJpaRepository.findByUserIdAndNotificationTypeAndStatus(
                    100L, NotificationType.ORDER_PAYMENT_COMPLETED, EntityStatus.ACTIVE
            )).thenReturn(Optional.empty());

            // when
            Optional<NotificationPreference> result = adapter.findByUserIdAndNotificationType(
                    100L, NotificationType.ORDER_PAYMENT_COMPLETED
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllDistinctUserIds")
    class FindAllDistinctUserIds {

        @Test
        @DisplayName("고유 사용자 ID 목록을 반환한다")
        void returnsDistinctUserIds() {
            // given
            when(notificationPreferenceJpaRepository.findAllDistinctUserIds())
                    .thenReturn(List.of(100L, 200L, 300L));

            // when
            List<Long> result = adapter.findAllDistinctUserIds();

            // then
            assertThat(result).containsExactly(100L, 200L, 300L);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("존재하는 수신 설정을 업데이트한다")
        void updatesExistingPreference() {
            // given
            NotificationPreference preference = createDomainPreferenceWithId(1L, 100L);
            NotificationPreferenceJpaEntity entity = mock(NotificationPreferenceJpaEntity.class);
            when(notificationPreferenceJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            adapter.update(preference);

            // then
            verify(entity).updateFrom(preference);
        }
    }

    private NotificationPreferenceJpaEntity createMockEntity(Long id, Long userId) {
        NotificationPreferenceJpaEntity entity = mock(NotificationPreferenceJpaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getUserId()).thenReturn(userId);
        when(entity.getNotificationType()).thenReturn(NotificationType.ORDER_PAYMENT_COMPLETED);
        when(entity.isEnabled()).thenReturn(true);
        when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
        return entity;
    }

    private NotificationPreference createDomainPreference(Long userId) {
        return NotificationPreference.from(
                NotificationPreferenceCreateState.builder()
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .enabled(true)
                        .build()
        );
    }

    private NotificationPreference createDomainPreferenceWithId(Long id, Long userId) {
        return NotificationPreference.from(
                NotificationPreferenceSnapshotState.builder()
                        .id(id)
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .enabled(true)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
