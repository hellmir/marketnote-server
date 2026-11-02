package com.personal.marketnote.notification.adapter.out.persistence.preference.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.preference.entity.NotificationPreferenceJpaEntity;
import com.personal.marketnote.notification.domain.preference.NotificationPreference;
import com.personal.marketnote.notification.domain.preference.NotificationPreferenceCreateState;
import com.personal.marketnote.notification.domain.template.NotificationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationPreferenceJpaRepositoryTest {

    @Autowired
    private NotificationPreferenceJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("사용자 ID와 상태로 수신 설정 목록을 조회한다")
    void shouldFindAllByUserIdAndStatus() {
        // given
        persistPreference(100L, NotificationType.ORDER_PAYMENT_COMPLETED, true);
        persistPreference(100L, NotificationType.SHIPPING_STARTED, true);

        // when
        List<NotificationPreferenceJpaEntity> result = repository.findAllByUserIdAndStatus(100L, EntityStatus.ACTIVE);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("사용자 ID와 알림 유형과 상태로 수신 설정을 조회한다")
    void shouldFindByUserIdAndNotificationTypeAndStatus() {
        // given
        persistPreference(100L, NotificationType.ORDER_PAYMENT_COMPLETED, true);

        // when
        Optional<NotificationPreferenceJpaEntity> result = repository.findByUserIdAndNotificationTypeAndStatus(
                100L, NotificationType.ORDER_PAYMENT_COMPLETED, EntityStatus.ACTIVE
        );

        // then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("활성 + enabled인 수신 설정만 조회한다")
    void shouldFindEnabledByUserIdsAndNotificationType() {
        // given
        persistPreference(100L, NotificationType.ORDER_PAYMENT_COMPLETED, true);
        persistPreference(200L, NotificationType.ORDER_PAYMENT_COMPLETED, false);
        persistPreference(300L, NotificationType.ORDER_PAYMENT_COMPLETED, true);

        // when
        List<NotificationPreferenceJpaEntity> result = repository
                .findByUserIdInAndNotificationTypeAndStatusAndEnabledTrue(
                        List.of(100L, 200L, 300L), NotificationType.ORDER_PAYMENT_COMPLETED, EntityStatus.ACTIVE
                );

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("고유 사용자 ID 목록을 조회한다")
    void shouldFindAllDistinctUserIds() {
        // given
        persistPreference(100L, NotificationType.ORDER_PAYMENT_COMPLETED, true);
        persistPreference(100L, NotificationType.SHIPPING_STARTED, true);
        persistPreference(200L, NotificationType.ORDER_PAYMENT_COMPLETED, true);

        // when
        List<Long> result = repository.findAllDistinctUserIds();

        // then
        assertThat(result).containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    @DisplayName("동의 재알림 대상을 조회한다")
    void shouldFindConsentReminderDue() {
        // given
        persistPreference(100L, NotificationType.ORDER_PAYMENT_COMPLETED, true);
        entityManager.createNativeQuery(
                "UPDATE notification_preference SET consented_at = :oldDate WHERE user_id = 100"
        ).setParameter("oldDate", LocalDateTime.of(2025, 1, 1, 0, 0)).executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // when
        List<NotificationPreferenceJpaEntity> result = repository.findConsentReminderDue(
                LocalDateTime.of(2026, 1, 1, 0, 0), EntityStatus.ACTIVE
        );

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(1);
    }

    private void persistPreference(Long userId, NotificationType type, boolean enabled) {
        NotificationPreference domain = NotificationPreference.from(
                NotificationPreferenceCreateState.builder()
                        .userId(userId)
                        .notificationType(type)
                        .enabled(enabled)
                        .build()
        );
        NotificationPreferenceJpaEntity entity = NotificationPreferenceJpaEntity.from(domain);
        repository.saveAndFlush(entity);
        entityManager.clear();
    }
}
