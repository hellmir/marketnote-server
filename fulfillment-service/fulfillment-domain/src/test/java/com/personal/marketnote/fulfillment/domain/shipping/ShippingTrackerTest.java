package com.personal.marketnote.fulfillment.domain.shipping;

import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.domain.exception.InvalidShippingStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ShippingTracker 테스트")
class ShippingTrackerTest {

    @Nested
    @DisplayName("생성 (from CreateState)")
    class CreateTests {

        @Test
        @DisplayName("orderId로 ShippingTracker를 생성하면 PREPARING 상태이고 폴링이 활성화된다")
        void createWithOrderId() {
            ShippingTrackerCreateState state = ShippingTrackerCreateState.builder()
                    .orderId(100L)
                    .build();

            ShippingTracker tracker = ShippingTracker.from(state);

            assertThat(tracker.getOrderId()).isEqualTo(100L);
            assertThat(tracker.isPreparing()).isTrue();
            assertThat(tracker.isPollingActive()).isTrue();
            assertThat(tracker.getTrackingNumber()).isNull();
            assertThat(tracker.getCarrierCode()).isNull();
        }

        @Test
        @DisplayName("orderId가 null이면 예외가 발생한다")
        void createWithNullOrderId() {
            ShippingTrackerCreateState state = ShippingTrackerCreateState.builder()
                    .orderId(null)
                    .build();

            assertThatThrownBy(() -> ShippingTracker.from(state))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    @Nested
    @DisplayName("복원 (from SnapshotState)")
    class SnapshotTests {

        @Test
        @DisplayName("SnapshotState로 모든 필드가 복원된다")
        void restoreFromSnapshot() {
            LocalDateTime now = LocalDateTime.of(2026, 4, 9, 10, 0);
            ShippingTrackerSnapshotState state = ShippingTrackerSnapshotState.builder()
                    .id(1L)
                    .orderId(100L)
                    .trackingNumber("1234567890")
                    .carrierCode("CJ")
                    .shippingStatus(ShippingStatus.SHIPPING)
                    .pollingActive(true)
                    .lastPolledAt(now)
                    .createdAt(now.minusDays(1))
                    .modifiedAt(now)
                    .build();

            ShippingTracker tracker = ShippingTracker.from(state);

            assertThat(tracker.getId()).isEqualTo(1L);
            assertThat(tracker.getOrderId()).isEqualTo(100L);
            assertThat(tracker.getTrackingNumber()).isEqualTo("1234567890");
            assertThat(tracker.getCarrierCode()).isEqualTo("CJ");
            assertThat(tracker.isShipping()).isTrue();
            assertThat(tracker.isPollingActive()).isTrue();
            assertThat(tracker.getLastPolledAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("startShipping - 배송 시작")
    class StartShippingTests {

        @Test
        @DisplayName("PREPARING 상태에서 배송을 시작하면 SHIPPING 상태가 되고 송장번호가 설정된다")
        void startShippingFromPreparing() {
            ShippingTracker tracker = createPreparingTracker();

            tracker.startShipping("1234567890", "CJ");

            assertThat(tracker.isShipping()).isTrue();
            assertThat(tracker.getTrackingNumber()).isEqualTo("1234567890");
            assertThat(tracker.getCarrierCode()).isEqualTo("CJ");
        }

        @Test
        @DisplayName("SHIPPING 상태에서 배송을 시작하면 예외가 발생한다")
        void startShippingFromShipping() {
            ShippingTracker tracker = createShippingTracker();

            assertThatThrownBy(() -> tracker.startShipping("9999999999", "HANJIN"))
                    .isInstanceOf(InvalidShippingStatusTransitionException.class);
        }

        @Test
        @DisplayName("송장번호가 null이면 예외가 발생한다")
        void startShippingWithNullTrackingNumber() {
            ShippingTracker tracker = createPreparingTracker();

            assertThatThrownBy(() -> tracker.startShipping(null, "CJ"))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("택배사 코드가 null이면 예외가 발생한다")
        void startShippingWithNullCarrierCode() {
            ShippingTracker tracker = createPreparingTracker();

            assertThatThrownBy(() -> tracker.startShipping("1234567890", null))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    @Nested
    @DisplayName("completeDelivery - 배송 완료")
    class CompleteDeliveryTests {

        @Test
        @DisplayName("SHIPPING 상태에서 배송을 완료하면 DELIVERED 상태가 되고 폴링이 비활성화된다")
        void completeDeliveryFromShipping() {
            ShippingTracker tracker = createShippingTracker();

            tracker.completeDelivery();

            assertThat(tracker.isDelivered()).isTrue();
            assertThat(tracker.isPollingActive()).isFalse();
        }

        @Test
        @DisplayName("PREPARING 상태에서 배송을 완료하면 예외가 발생한다")
        void completeDeliveryFromPreparing() {
            ShippingTracker tracker = createPreparingTracker();

            assertThatThrownBy(() -> tracker.completeDelivery())
                    .isInstanceOf(InvalidShippingStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("cancel - 추적 종료")
    class CancelTests {

        @Test
        @DisplayName("PREPARING 상태에서 취소하면 CANCELLED 상태가 되고 폴링이 비활성화된다")
        void cancelFromPreparing() {
            ShippingTracker tracker = createPreparingTracker();

            tracker.cancel();

            assertThat(tracker.isCancelled()).isTrue();
            assertThat(tracker.isPollingActive()).isFalse();
        }

        @Test
        @DisplayName("SHIPPING 상태에서 취소하면 CANCELLED 상태가 되고 폴링이 비활성화된다")
        void cancelFromShipping() {
            ShippingTracker tracker = createShippingTracker();

            tracker.cancel();

            assertThat(tracker.isCancelled()).isTrue();
            assertThat(tracker.isPollingActive()).isFalse();
        }

        @Test
        @DisplayName("DELIVERED 상태에서 취소하면 예외가 발생한다")
        void cancelFromDelivered() {
            ShippingTracker tracker = createDeliveredTracker();

            assertThatThrownBy(() -> tracker.cancel())
                    .isInstanceOf(InvalidShippingStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("startReturnShipping - 반품 회수 시작")
    class StartReturnShippingTests {

        @Test
        @DisplayName("DELIVERED 상태에서 반품 회수를 시작하면 RETURN_SHIPPING 상태가 되고 폴링이 재활성화된다")
        void startReturnShippingFromDelivered() {
            ShippingTracker tracker = createDeliveredTracker();

            tracker.startReturnShipping();

            assertThat(tracker.isReturnShipping()).isTrue();
            assertThat(tracker.isPollingActive()).isTrue();
        }

        @Test
        @DisplayName("SHIPPING 상태에서 반품 회수를 시작하면 예외가 발생한다")
        void startReturnShippingFromShipping() {
            ShippingTracker tracker = createShippingTracker();

            assertThatThrownBy(() -> tracker.startReturnShipping())
                    .isInstanceOf(InvalidShippingStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("completeReturnDelivery - 반품 회수 완료")
    class CompleteReturnDeliveryTests {

        @Test
        @DisplayName("RETURN_SHIPPING 상태에서 반품 회수를 완료하면 RETURN_DELIVERED 상태가 되고 폴링이 비활성화된다")
        void completeReturnDeliveryFromReturnShipping() {
            ShippingTracker tracker = createReturnShippingTracker();

            tracker.completeReturnDelivery();

            assertThat(tracker.isReturnDelivered()).isTrue();
            assertThat(tracker.isPollingActive()).isFalse();
        }

        @Test
        @DisplayName("DELIVERED 상태에서 반품 회수를 완료하면 예외가 발생한다")
        void completeReturnDeliveryFromDelivered() {
            ShippingTracker tracker = createDeliveredTracker();

            assertThatThrownBy(() -> tracker.completeReturnDelivery())
                    .isInstanceOf(InvalidShippingStatusTransitionException.class);
        }
    }

    @Nested
    @DisplayName("updateLastPolledAt - 폴링 시각 갱신")
    class UpdateLastPolledAtTests {

        @Test
        @DisplayName("폴링 시각을 갱신하면 lastPolledAt이 업데이트된다")
        void updateLastPolledAt() {
            ShippingTracker tracker = createPreparingTracker();
            LocalDateTime polledAt = LocalDateTime.of(2026, 4, 9, 12, 0);

            tracker.updateLastPolledAt(polledAt);

            assertThat(tracker.getLastPolledAt()).isEqualTo(polledAt);
        }

        @Test
        @DisplayName("폴링 시각이 null이면 예외가 발생한다")
        void updateLastPolledAtWithNull() {
            ShippingTracker tracker = createPreparingTracker();

            assertThatThrownBy(() -> tracker.updateLastPolledAt(null))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    // --- 테스트 헬퍼 ---

    private ShippingTracker createPreparingTracker() {
        return ShippingTracker.from(ShippingTrackerCreateState.builder()
                .orderId(100L)
                .build());
    }

    private ShippingTracker createShippingTracker() {
        ShippingTracker tracker = createPreparingTracker();
        tracker.startShipping("1234567890", "CJ");
        return tracker;
    }

    private ShippingTracker createDeliveredTracker() {
        ShippingTracker tracker = createShippingTracker();
        tracker.completeDelivery();
        return tracker;
    }

    private ShippingTracker createReturnShippingTracker() {
        ShippingTracker tracker = createDeliveredTracker();
        tracker.startReturnShipping();
        return tracker;
    }
}
