package com.personal.marketnote.fulfillment.domain.delivery;

import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FulfillmentDeliveryRegistration 테스트")
class FulfillmentDeliveryRegistrationTest {

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("orderId가 유효하면 REGISTERED 상태로 생성된다")
        void shouldCreateWithRegisteredStatusWhenOrderIdIsValid() {
            // given
            FulfillmentDeliveryRegistrationCreateState state = FulfillmentDeliveryRegistrationCreateState.builder()
                    .orderId(1L)
                    .build();

            // when
            FulfillmentDeliveryRegistration registration = FulfillmentDeliveryRegistration.from(state);

            // then
            assertThat(registration.getOrderId()).isEqualTo(1L);
            assertThat(registration.getWorkStatus()).isEqualTo(FulfillmentWorkStatus.REGISTERED);
        }

        @Test
        @DisplayName("orderId가 null이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowExceptionWhenOrderIdIsNull() {
            // given
            FulfillmentDeliveryRegistrationCreateState state = FulfillmentDeliveryRegistrationCreateState.builder()
                    .orderId(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> FulfillmentDeliveryRegistration.from(state))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState의 모든 필드가 그대로 복원된다")
        void shouldRestoreAllFieldsFromSnapshotState() {
            // given
            LocalDateTime createdAt = LocalDateTime.of(2026, 4, 11, 10, 0, 0);
            FulfillmentDeliveryRegistrationSnapshotState state = FulfillmentDeliveryRegistrationSnapshotState.builder()
                    .id(100L)
                    .orderId(200L)
                    .workStatus(FulfillmentWorkStatus.PICKING)
                    .createdAt(createdAt)
                    .build();

            // when
            FulfillmentDeliveryRegistration registration = FulfillmentDeliveryRegistration.from(state);

            // then
            assertThat(registration.getId()).isEqualTo(100L);
            assertThat(registration.getOrderId()).isEqualTo(200L);
            assertThat(registration.getWorkStatus()).isEqualTo(FulfillmentWorkStatus.PICKING);
            assertThat(registration.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("isCancellable()")
    class IsCancellable {

        @Test
        @DisplayName("취소 가능 상태이면 true를 반환한다")
        void shouldReturnTrueWhenStatusIsCancellable() {
            // given
            FulfillmentDeliveryRegistration registration = FulfillmentDeliveryRegistration.from(
                    FulfillmentDeliveryRegistrationSnapshotState.builder()
                            .id(1L)
                            .orderId(1L)
                            .workStatus(FulfillmentWorkStatus.REGISTERED)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            // when & then
            assertThat(registration.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("취소 불가 상태이면 false를 반환한다")
        void shouldReturnFalseWhenStatusIsNotCancellable() {
            // given
            FulfillmentDeliveryRegistration registration = FulfillmentDeliveryRegistration.from(
                    FulfillmentDeliveryRegistrationSnapshotState.builder()
                            .id(1L)
                            .orderId(1L)
                            .workStatus(FulfillmentWorkStatus.RELEASED)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            // when & then
            assertThat(registration.isCancellable()).isFalse();
        }
    }
}
