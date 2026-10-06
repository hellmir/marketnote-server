package com.personal.marketnote.notification.adapter.out.persistence.notification.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.notification.entity.NotificationJpaEntity;
import com.personal.marketnote.notification.domain.notification.*;
import com.personal.marketnote.notification.domain.template.NotificationType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationJpaRepositoryTest {

    @Autowired
    private NotificationJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("ID와 상태로 알림을 조회한다")
    void shouldFindByIdAndStatus() {
        // given
        NotificationJpaEntity entity = persistNotification(100L, SendStatus.SENT);

        // when
        Optional<NotificationJpaEntity> result = repository.findByIdAndStatus(entity.getId(), EntityStatus.ACTIVE);

        // then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("커서 기반으로 사용자별 알림 목록을 조회한다")
    void shouldFindByUserIdWithCursor() {
        // given
        persistNotification(100L, SendStatus.SENT);
        persistNotification(100L, SendStatus.SENT);
        persistNotification(100L, SendStatus.SENT);

        // when
        List<NotificationJpaEntity> result = repository.findByUserIdWithCursor(
                100L, EntityStatus.ACTIVE, -1L, PageRequest.of(0, 10)
        );

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("사용자별 활성 알림 수를 반환한다")
    void shouldCountByUserIdAndStatus() {
        // given
        persistNotification(100L, SendStatus.SENT);
        persistNotification(100L, SendStatus.SENT);

        // when
        long count = repository.countByUserIdAndStatus(100L, EntityStatus.ACTIVE);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("읽지 않은 알림 수를 반환한다")
    void shouldCountUnreadByUserIdAndStatus() {
        // given
        persistNotification(100L, SendStatus.SENT);
        persistNotification(100L, SendStatus.SENT);

        // when
        long count = repository.countByUserIdAndStatusAndIsRead(100L, EntityStatus.ACTIVE, false);

        // then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("예약 발송 대상 알림을 조회한다")
    void shouldFindScheduledNotificationsDue() {
        // given
        persistScheduledNotification(100L, LocalDateTime.of(2026, 4, 1, 10, 0));

        // when
        List<NotificationJpaEntity> result = repository.findScheduledNotificationsDue(
                SendStatus.SCHEDULED, LocalDateTime.of(2026, 4, 15, 0, 0), EntityStatus.ACTIVE
        );

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("만료 알림을 비활성화한다")
    void shouldDeactivateExpiredNotifications() {
        // given
        persistNotification(100L, SendStatus.SENT);
        entityManager.createNativeQuery("UPDATE notification SET created_at = :oldDate WHERE user_id = 100")
                .setParameter("oldDate", LocalDateTime.of(2025, 1, 1, 0, 0))
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // when
        int deactivated = repository.deactivateExpiredNotifications(
                LocalDateTime.of(2026, 1, 1, 0, 0), EntityStatus.ACTIVE, EntityStatus.INACTIVE
        );

        // then
        assertThat(deactivated).isGreaterThanOrEqualTo(1);
    }

    private NotificationJpaEntity persistNotification(Long userId, SendStatus sendStatus) {
        Notification domain = Notification.from(
                NotificationCreateState.builder()
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title("테스트 알림")
                        .body("테스트 알림 본문")
                        .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                        .build()
        );
        NotificationJpaEntity entity = NotificationJpaEntity.from(domain);
        NotificationJpaEntity saved = repository.saveAndFlush(entity);
        entityManager.clear();
        return saved;
    }

    private NotificationJpaEntity persistScheduledNotification(Long userId, LocalDateTime scheduledAt) {
        Notification domain = Notification.from(
                NotificationCreateState.builder()
                        .userId(userId)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .title("예약 알림")
                        .body("예약 알림 본문")
                        .deliveryChannel(DeliveryChannel.PUSH_ONLY)
                        .scheduledAt(scheduledAt)
                        .build()
        );
        NotificationJpaEntity entity = NotificationJpaEntity.from(domain);
        NotificationJpaEntity saved = repository.saveAndFlush(entity);
        entityManager.clear();
        return saved;
    }
}
