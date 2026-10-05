package com.personal.marketnote.fulfillment.adapter.out.persistence.shipping;

import com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.entity.ShippingTrackerJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.shipping.repository.ShippingTrackerJpaRepository;
import com.personal.marketnote.fulfillment.domain.exception.ShippingTrackerNotFoundException;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingStatus;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTrackerSnapshotState;
import com.personal.marketnote.fulfillment.domain.shipping.TrackingNumber;
import com.personal.marketnote.fulfillment.domain.shipping.CarrierCode;
import com.personal.marketnote.fulfillment.exception.ShippingTrackerAlreadyExistsException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingTrackerPersistenceAdapter 테스트")
class ShippingTrackerPersistenceAdapterTest {

    @InjectMocks
    private ShippingTrackerPersistenceAdapter adapter;

    @Mock
    private ShippingTrackerJpaRepository repository;

    private ShippingTracker buildTracker(Long id, Long orderId, ShippingStatus status, boolean pollingActive) {
        return ShippingTracker.from(
                ShippingTrackerSnapshotState.builder()
                        .id(id)
                        .orderId(orderId)
                        .buyerId(1L)
                        .trackingNumber(TrackingNumber.of("TRACK001"))
                        .carrierCode(CarrierCode.of("CJ"))
                        .shippingStatus(status)
                        .pollingActive(pollingActive)
                        .lastPolledAt(LocalDateTime.of(2026, 4, 14, 10, 0))
                        .createdAt(LocalDateTime.of(2026, 4, 14, 9, 0))
                        .modifiedAt(LocalDateTime.of(2026, 4, 14, 10, 0))
                        .build()
        );
    }

    private ShippingTrackerJpaEntity buildEntity(Long id, Long orderId, ShippingStatus status, boolean pollingActive) {
        ShippingTracker tracker = buildTracker(id, orderId, status, pollingActive);
        return ShippingTrackerJpaEntity.from(tracker);
    }

    @Nested
    @DisplayName("save 성공")
    class SaveSuccess {

        @Test
        @DisplayName("배송 추적 도메인 객체를 JPA 엔티티로 변환하여 저장한다")
        void shouldSaveShippingTrackerAsJpaEntity() {
            // given
            ShippingTracker tracker = buildTracker(null, 100L, ShippingStatus.PREPARING, true);

            // when
            adapter.save(tracker);

            // then
            verify(repository).saveAndFlush(any(ShippingTrackerJpaEntity.class));
        }
    }

    @Nested
    @DisplayName("save 실패")
    class SaveFailure {

        @Test
        @DisplayName("동일 orderId로 중복 저장 시 ShippingTrackerAlreadyExistsException이 발생한다")
        void shouldThrowAlreadyExistsExceptionWhenDuplicateOrderId() {
            // given
            ShippingTracker tracker = buildTracker(null, 100L, ShippingStatus.PREPARING, true);
            when(repository.saveAndFlush(any(ShippingTrackerJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

            // when & then
            assertThatThrownBy(() -> adapter.save(tracker))
                    .isInstanceOf(ShippingTrackerAlreadyExistsException.class);
        }
    }

    @Nested
    @DisplayName("findAllPollingActive")
    class FindAllPollingActive {

        @Test
        @DisplayName("폴링 활성화된 배송 추적 목록을 도메인 객체로 변환하여 반환한다")
        void shouldReturnPollingActiveTrackersAsDomainObjects() {
            // given
            ShippingTrackerJpaEntity entity1 = buildEntity(1L, 100L, ShippingStatus.SHIPPING, true);
            ShippingTrackerJpaEntity entity2 = buildEntity(2L, 200L, ShippingStatus.PREPARING, true);
            when(repository.findByPollingActiveTrue()).thenReturn(List.of(entity1, entity2));

            // when
            List<ShippingTracker> result = adapter.findAllPollingActive();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getOrderId()).isEqualTo(100L);
            assertThat(result.get(1).getOrderId()).isEqualTo(200L);
        }

        @Test
        @DisplayName("폴링 활성화된 배송 추적이 없으면 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenNoPollingActiveTrackers() {
            // given
            when(repository.findByPollingActiveTrue()).thenReturn(List.of());

            // when
            List<ShippingTracker> result = adapter.findAllPollingActive();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("orderId에 해당하는 배송 추적이 존재하면 도메인 객체를 반환한다")
        void shouldReturnDomainWhenEntityExists() {
            // given
            ShippingTrackerJpaEntity entity = buildEntity(1L, 100L, ShippingStatus.SHIPPING, true);
            when(repository.findByOrderId(100L)).thenReturn(Optional.of(entity));

            // when
            Optional<ShippingTracker> result = adapter.findByOrderId(100L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getOrderId()).isEqualTo(100L);
            assertThat(result.get().getShippingStatus()).isEqualTo(ShippingStatus.SHIPPING);
        }

        @Test
        @DisplayName("orderId에 해당하는 배송 추적이 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenEntityNotFound() {
            // given
            when(repository.findByOrderId(999L)).thenReturn(Optional.empty());

            // when
            Optional<ShippingTracker> result = adapter.findByOrderId(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("id가 있는 배송 추적 도메인 객체를 업데이트한다")
        void shouldUpdateShippingTrackerWhenIdExists() {
            // given
            ShippingTracker tracker = buildTracker(1L, 100L, ShippingStatus.DELIVERED, false);

            // when
            adapter.update(tracker);

            // then
            verify(repository).saveAndFlush(any(ShippingTrackerJpaEntity.class));
        }

        @Test
        @DisplayName("id가 null인 배송 추적 업데이트 시 ShippingTrackerNotFoundException이 발생한다")
        void shouldThrowNotFoundExceptionWhenIdIsNull() {
            // given
            ShippingTracker tracker = buildTracker(null, 100L, ShippingStatus.PREPARING, true);

            // when & then
            assertThatThrownBy(() -> adapter.update(tracker))
                    .isInstanceOf(ShippingTrackerNotFoundException.class);
        }
    }
}
