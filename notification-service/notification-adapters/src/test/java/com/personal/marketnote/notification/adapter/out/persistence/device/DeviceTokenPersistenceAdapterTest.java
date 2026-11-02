package com.personal.marketnote.notification.adapter.out.persistence.device;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.device.entity.DeviceTokenJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.device.repository.DeviceTokenJpaRepository;
import com.personal.marketnote.notification.domain.device.*;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceTokenPersistenceAdapterTest {

    @InjectMocks
    private DeviceTokenPersistenceAdapter adapter;

    @Mock
    private DeviceTokenJpaRepository deviceTokenJpaRepository;

    @Nested
    @DisplayName("findActiveByUserId")
    class FindActiveByUserId {

        @Test
        @DisplayName("활성 디바이스 토큰 목록을 반환한다")
        void returnsActiveDeviceTokens() {
            // given
            DeviceTokenJpaEntity entity = createMockEntity(1L, 100L, "device-1");
            when(deviceTokenJpaRepository.findByUserIdAndStatus(100L, EntityStatus.ACTIVE))
                    .thenReturn(List.of(entity));

            // when
            List<DeviceToken> result = adapter.findActiveByUserId(100L);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("디바이스 토큰이 없으면 빈 목록을 반환한다")
        void returnsEmptyListWhenNone() {
            // given
            when(deviceTokenJpaRepository.findByUserIdAndStatus(100L, EntityStatus.ACTIVE))
                    .thenReturn(List.of());

            // when
            List<DeviceToken> result = adapter.findActiveByUserId(100L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActiveByDeviceId")
    class FindActiveByDeviceId {

        @Test
        @DisplayName("존재하면 도메인 객체를 반환한다")
        void returnsDomainWhenFound() {
            // given
            DeviceTokenJpaEntity entity = createMockEntity(1L, 100L, "device-1");
            when(deviceTokenJpaRepository.findByDeviceIdAndStatus("device-1", EntityStatus.ACTIVE))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<DeviceToken> result = adapter.findActiveByDeviceId("device-1");

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("존재하지 않으면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(deviceTokenJpaRepository.findByDeviceIdAndStatus("unknown", EntityStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when
            Optional<DeviceToken> result = adapter.findActiveByDeviceId("unknown");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("디바이스 토큰을 저장하고 ID를 반환한다")
        void savesAndReturnsId() {
            // given
            DeviceToken token = createDomainToken();
            DeviceTokenJpaEntity savedEntity = mock(DeviceTokenJpaEntity.class);
            when(savedEntity.getId()).thenReturn(1L);
            when(deviceTokenJpaRepository.save(any(DeviceTokenJpaEntity.class))).thenReturn(savedEntity);

            // when
            Long savedId = adapter.save(token);

            // then
            assertThat(savedId).isEqualTo(1L);
        }

        @Test
        @DisplayName("중복 디바이스 ID이면 DuplicateDeviceTokenException이 발생한다")
        void throwsExceptionOnDuplicate() {
            // given
            DeviceToken token = createDomainToken();
            when(deviceTokenJpaRepository.save(any(DeviceTokenJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate"));

            // when & then
            assertThatThrownBy(() -> adapter.save(token))
                    .isInstanceOf(DuplicateDeviceTokenException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("존재하는 토큰을 업데이트한다")
        void updatesExistingToken() {
            // given
            DeviceToken token = createDomainTokenWithId(1L);
            DeviceTokenJpaEntity entity = mock(DeviceTokenJpaEntity.class);
            when(entity.getId()).thenReturn(1L);
            when(deviceTokenJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            Long updatedId = adapter.update(token);

            // then
            assertThat(updatedId).isEqualTo(1L);
            verify(entity).updateFrom(token);
        }

        @Test
        @DisplayName("존재하지 않으면 DeviceTokenNotFoundException이 발생한다")
        void throwsExceptionWhenNotFound() {
            // given
            DeviceToken token = createDomainTokenWithId(999L);
            when(deviceTokenJpaRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adapter.update(token))
                    .isInstanceOf(DeviceTokenNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deactivateStaleTokens")
    class DeactivateStaleTokens {

        @Test
        @DisplayName("임계시간 이전 토큰을 비활성화하고 건수를 반환한다")
        void deactivatesAndReturnsCount() {
            // given
            LocalDateTime threshold = LocalDateTime.of(2026, 1, 1, 0, 0);
            when(deviceTokenJpaRepository.deactivateStaleTokens(threshold, EntityStatus.ACTIVE, EntityStatus.INACTIVE))
                    .thenReturn(5);

            // when
            int count = adapter.deactivateStaleTokens(threshold);

            // then
            assertThat(count).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("ID로 디바이스 토큰을 삭제한다")
        void deletesById() {
            // when
            adapter.deleteById(1L);

            // then
            verify(deviceTokenJpaRepository).deleteById(1L);
            verify(deviceTokenJpaRepository).flush();
        }
    }

    private DeviceTokenJpaEntity createMockEntity(Long id, Long userId, String deviceId) {
        DeviceTokenJpaEntity entity = mock(DeviceTokenJpaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getUserId()).thenReturn(userId);
        when(entity.getToken()).thenReturn("fcm-token-123");
        when(entity.getPlatform()).thenReturn(Platform.ANDROID);
        when(entity.getDeviceId()).thenReturn(deviceId);
        when(entity.getLastUsedAt()).thenReturn(LocalDateTime.of(2026, 4, 1, 10, 0));
        when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
        return entity;
    }

    private DeviceToken createDomainToken() {
        return DeviceToken.from(
                DeviceTokenCreateState.builder()
                        .userId(100L)
                        .token("fcm-token-123")
                        .platform(Platform.ANDROID)
                        .deviceId("device-1")
                        .build()
        );
    }

    private DeviceToken createDomainTokenWithId(Long id) {
        return DeviceToken.from(
                DeviceTokenSnapshotState.builder()
                        .id(id)
                        .userId(100L)
                        .token("fcm-token-123")
                        .platform(Platform.ANDROID)
                        .deviceId("device-1")
                        .lastUsedAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
