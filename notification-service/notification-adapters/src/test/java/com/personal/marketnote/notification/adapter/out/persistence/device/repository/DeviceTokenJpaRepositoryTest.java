package com.personal.marketnote.notification.adapter.out.persistence.device.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.device.entity.DeviceTokenJpaEntity;
import com.personal.marketnote.notification.domain.device.DeviceToken;
import com.personal.marketnote.notification.domain.device.DeviceTokenCreateState;
import com.personal.marketnote.notification.domain.device.Platform;
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
class DeviceTokenJpaRepositoryTest {

    @Autowired
    private DeviceTokenJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("디바이스 ID와 상태로 토큰을 조회한다")
    void shouldFindByDeviceIdAndStatus() {
        // given
        persistToken(100L, "device-001", "fcm-token-001");

        // when
        Optional<DeviceTokenJpaEntity> result = repository.findByDeviceIdAndStatus("device-001", EntityStatus.ACTIVE);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("fcm-token-001");
    }

    @Test
    @DisplayName("사용자 ID와 상태로 토큰 목록을 조회한다")
    void shouldFindByUserIdAndStatus() {
        // given
        persistToken(100L, "device-100a", "token-a");
        persistToken(100L, "device-100b", "token-b");
        persistToken(200L, "device-200", "token-c");

        // when
        List<DeviceTokenJpaEntity> result = repository.findByUserIdAndStatus(100L, EntityStatus.ACTIVE);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("사용자 ID 목록으로 활성 토큰을 일괄 조회한다")
    void shouldFindByUserIdInAndStatus() {
        // given
        persistToken(100L, "device-batch-1", "token-1");
        persistToken(200L, "device-batch-2", "token-2");
        persistToken(300L, "device-batch-3", "token-3");

        // when
        List<DeviceTokenJpaEntity> result = repository.findByUserIdInAndStatus(
                List.of(100L, 200L), EntityStatus.ACTIVE
        );

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("임계시간 이전의 stale 토큰을 비활성화한다")
    void shouldDeactivateStaleTokens() {
        // given
        persistToken(100L, "device-stale", "token-stale");
        entityManager.createNativeQuery(
                "UPDATE device_token SET last_used_at = :oldDate WHERE device_id = 'device-stale'"
        ).setParameter("oldDate", LocalDateTime.of(2025, 1, 1, 0, 0)).executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // when
        int deactivated = repository.deactivateStaleTokens(
                LocalDateTime.of(2026, 1, 1, 0, 0), EntityStatus.ACTIVE, EntityStatus.INACTIVE
        );

        // then
        assertThat(deactivated).isGreaterThanOrEqualTo(1);
    }

    private DeviceTokenJpaEntity persistToken(Long userId, String deviceId, String token) {
        DeviceToken domain = DeviceToken.from(
                DeviceTokenCreateState.builder()
                        .userId(userId)
                        .token(token)
                        .platform(Platform.ANDROID)
                        .deviceId(deviceId)
                        .lastUsedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                        .build()
        );
        DeviceTokenJpaEntity entity = DeviceTokenJpaEntity.from(domain);
        DeviceTokenJpaEntity saved = repository.saveAndFlush(entity);
        entityManager.clear();
        return saved;
    }
}
