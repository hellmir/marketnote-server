package com.personal.marketnote.fulfillment.adapter.out.persistence.delivery;

import com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.entity.FulfillmentDeliveryRegistrationJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.delivery.repository.FulfillmentDeliveryRegistrationJpaRepository;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistrationSnapshotState;
import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentWorkStatus;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryAlreadyRegisteredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentDeliveryRegistrationPersistenceAdapter 테스트")
class FulfillmentDeliveryRegistrationPersistenceAdapterTest {

    @InjectMocks
    private FulfillmentDeliveryRegistrationPersistenceAdapter adapter;

    @Mock
    private FulfillmentDeliveryRegistrationJpaRepository repository;

    private FulfillmentDeliveryRegistration buildRegistration(Long id, Long orderId, FulfillmentWorkStatus workStatus) {
        return FulfillmentDeliveryRegistration.from(
                FulfillmentDeliveryRegistrationSnapshotState.builder()
                        .id(id)
                        .orderId(orderId)
                        .workStatus(workStatus)
                        .createdAt(LocalDateTime.of(2026, 4, 14, 10, 0))
                        .build()
        );
    }

    @Nested
    @DisplayName("save 성공")
    class SaveSuccess {

        @Test
        @DisplayName("배송 등록 도메인 객체를 JPA 엔티티로 변환하여 저장한다")
        void shouldSaveDeliveryRegistrationAsJpaEntity() {
            // given
            FulfillmentDeliveryRegistration registration = buildRegistration(null, 100L, FulfillmentWorkStatus.REGISTERED);

            // when
            adapter.save(registration);

            // then
            verify(repository).saveAndFlush(any(FulfillmentDeliveryRegistrationJpaEntity.class));
        }
    }

    @Nested
    @DisplayName("save 실패")
    class SaveFailure {

        @Test
        @DisplayName("동일 orderId로 중복 저장 시 FulfillmentDeliveryAlreadyRegisteredException이 발생한다")
        void shouldThrowAlreadyRegisteredExceptionWhenDuplicateOrderId() {
            // given
            FulfillmentDeliveryRegistration registration = buildRegistration(null, 100L, FulfillmentWorkStatus.REGISTERED);
            when(repository.saveAndFlush(any(FulfillmentDeliveryRegistrationJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

            // when & then
            assertThatThrownBy(() -> adapter.save(registration))
                    .isInstanceOf(FulfillmentDeliveryAlreadyRegisteredException.class);
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("orderId에 해당하는 배송 등록이 존재하면 도메인 객체를 반환한다")
        void shouldReturnDomainWhenEntityExists() {
            // given
            FulfillmentDeliveryRegistrationJpaEntity entity = FulfillmentDeliveryRegistrationJpaEntity.from(
                    buildRegistration(1L, 200L, FulfillmentWorkStatus.REGISTERED)
            );
            when(repository.findByOrderId(200L)).thenReturn(Optional.of(entity));

            // when
            Optional<FulfillmentDeliveryRegistration> result = adapter.findByOrderId(200L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getOrderId()).isEqualTo(200L);
            assertThat(result.get().getWorkStatus()).isEqualTo(FulfillmentWorkStatus.REGISTERED);
        }

        @Test
        @DisplayName("orderId에 해당하는 배송 등록이 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenEntityNotFound() {
            // given
            when(repository.findByOrderId(999L)).thenReturn(Optional.empty());

            // when
            Optional<FulfillmentDeliveryRegistration> result = adapter.findByOrderId(999L);

            // then
            assertThat(result).isEmpty();
        }
    }
}
