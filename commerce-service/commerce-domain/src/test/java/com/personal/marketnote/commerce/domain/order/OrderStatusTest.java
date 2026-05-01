package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Nested
    @DisplayName("구매자 허용 상태 검증 (isBuyerAllowed)")
    class IsBuyerAllowedTest {

        @Test
        @DisplayName("CONFIRMED는 구매자가 변경 가능한 상태이다")
        void confirmed_isBuyerAllowed() {
            assertThat(OrderStatus.CONFIRMED.isBuyerAllowed()).isTrue();
        }

        @Test
        @DisplayName("RETURN_REQUESTED는 구매자가 변경 가능한 상태이다")
        void returnRequested_isBuyerAllowed() {
            assertThat(OrderStatus.RETURN_REQUESTED.isBuyerAllowed()).isTrue();
        }

        @Test
        @DisplayName("PAID는 구매자가 변경할 수 없는 상태이다")
        void paid_isNotBuyerAllowed() {
            assertThat(OrderStatus.PAID.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("PAYMENT_PENDING는 구매자가 변경할 수 없는 상태이다")
        void paymentPending_isNotBuyerAllowed() {
            assertThat(OrderStatus.PAYMENT_PENDING.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("FAILED는 구매자가 변경할 수 없는 상태이다")
        void failed_isNotBuyerAllowed() {
            assertThat(OrderStatus.FAILED.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("PREPARING는 구매자가 변경할 수 없는 상태이다")
        void preparing_isNotBuyerAllowed() {
            assertThat(OrderStatus.PREPARING.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("CANCEL_REQUESTED는 구매자가 변경 가능한 상태이다")
        void cancelRequested_isBuyerAllowed() {
            assertThat(OrderStatus.CANCEL_REQUESTED.isBuyerAllowed()).isTrue();
        }

        @Test
        @DisplayName("CANCELLED는 구매자가 변경 가능한 상태이다")
        void cancelled_isBuyerAllowed() {
            assertThat(OrderStatus.CANCELLED.isBuyerAllowed()).isTrue();
        }

        @Test
        @DisplayName("SHIPPING는 구매자가 변경할 수 없는 상태이다")
        void shipping_isNotBuyerAllowed() {
            assertThat(OrderStatus.SHIPPING.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("DELIVERED는 구매자가 변경할 수 없는 상태이다")
        void delivered_isNotBuyerAllowed() {
            assertThat(OrderStatus.DELIVERED.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("PARTIALLY_CONFIRMED는 구매자가 변경할 수 없는 상태이다")
        void partiallyConfirmed_isNotBuyerAllowed() {
            assertThat(OrderStatus.PARTIALLY_CONFIRMED.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("RETURN_IN_PROGRESS는 구매자가 변경할 수 없는 상태이다")
        void returnInProgress_isNotBuyerAllowed() {
            assertThat(OrderStatus.RETURN_IN_PROGRESS.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("PARTIALLY_RETURNED는 구매자가 변경할 수 없는 상태이다")
        void partiallyReturned_isNotBuyerAllowed() {
            assertThat(OrderStatus.PARTIALLY_RETURNED.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("RETURNED는 구매자가 변경할 수 없는 상태이다")
        void returned_isNotBuyerAllowed() {
            assertThat(OrderStatus.RETURNED.isBuyerAllowed()).isFalse();
        }
    }

    @Nested
    @DisplayName("상태 전이 가능 여부 검증 (canTransitionTo)")
    class CanTransitionToTest {

        @Test
        @DisplayName("PAYMENT_PENDING에서 PAID로 전이할 수 있다")
        void paymentPending_canTransitionTo_paid() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.PAID)).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 FAILED로 전이할 수 있다")
        void paymentPending_canTransitionTo_failed() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 CANCELLED로 전이할 수 있다")
        void paymentPending_canTransitionTo_cancelled() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 SHIPPING로 전이할 수 없다")
        void paymentPending_cannotTransitionTo_shipping() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.SHIPPING)).isFalse();
        }

        @Test
        @DisplayName("PAID에서 PREPARING로 전이할 수 있다")
        void paid_canTransitionTo_preparing() {
            assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PREPARING)).isTrue();
        }

        @Test
        @DisplayName("PAID에서 CANCEL_REQUESTED로 전이할 수 있다")
        void paid_canTransitionTo_cancelRequested() {
            assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCEL_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PAID에서 CANCELLED로 직접 전이할 수 없다")
        void paid_cannotTransitionTo_cancelled() {
            assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("PAID에서 SHIPPING로 전이할 수 없다")
        void paid_cannotTransitionTo_shipping() {
            assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPING)).isFalse();
        }

        @Test
        @DisplayName("PREPARING에서 SHIPPING로 전이할 수 있다")
        void preparing_canTransitionTo_shipping() {
            assertThat(OrderStatus.PREPARING.canTransitionTo(OrderStatus.SHIPPING)).isTrue();
        }

        @Test
        @DisplayName("PREPARING에서 CANCEL_REQUESTED로 전이할 수 있다")
        void preparing_canTransitionTo_cancelRequested() {
            assertThat(OrderStatus.PREPARING.canTransitionTo(OrderStatus.CANCEL_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PREPARING에서 CANCELLED로 직접 전이할 수 없다")
        void preparing_cannotTransitionTo_cancelled() {
            assertThat(OrderStatus.PREPARING.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("SHIPPING에서 DELIVERED로 전이할 수 있다")
        void shipping_canTransitionTo_delivered() {
            assertThat(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
        }

        @Test
        @DisplayName("SHIPPING에서 RETURN_REQUESTED로 전이할 수 있다")
        void shipping_canTransitionTo_returnRequested() {
            assertThat(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("SHIPPING에서 CANCELLED로 전이할 수 없다")
        void shipping_cannotTransitionTo_cancelled() {
            assertThat(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("DELIVERED에서 CONFIRMED로 전이할 수 있다")
        void delivered_canTransitionTo_confirmed() {
            assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        }

        @Test
        @DisplayName("DELIVERED에서 RETURN_REQUESTED로 전이할 수 있다")
        void delivered_canTransitionTo_returnRequested() {
            assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_CONFIRMED에서 CONFIRMED로 전이할 수 있다")
        void partiallyConfirmed_canTransitionTo_confirmed() {
            assertThat(OrderStatus.PARTIALLY_CONFIRMED.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_CONFIRMED에서 RETURN_REQUESTED로 전이할 수 있다")
        void partiallyConfirmed_canTransitionTo_returnRequested() {
            assertThat(OrderStatus.PARTIALLY_CONFIRMED.canTransitionTo(OrderStatus.RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("RETURN_REQUESTED에서 RETURN_IN_PROGRESS로 전이할 수 있다")
        void returnRequested_canTransitionTo_returnInProgress() {
            assertThat(OrderStatus.RETURN_REQUESTED.canTransitionTo(OrderStatus.RETURN_IN_PROGRESS)).isTrue();
        }

        @Test
        @DisplayName("RETURN_IN_PROGRESS에서 RETURNED로 전이할 수 있다")
        void returnInProgress_canTransitionTo_returned() {
            assertThat(OrderStatus.RETURN_IN_PROGRESS.canTransitionTo(OrderStatus.RETURNED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RETURNED에서 RETURN_REQUESTED로 전이할 수 있다")
        void partiallyReturned_canTransitionTo_returnRequested() {
            assertThat(OrderStatus.PARTIALLY_RETURNED.canTransitionTo(OrderStatus.RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RETURNED에서 RETURNED로 전이할 수 있다")
        void partiallyReturned_canTransitionTo_returned() {
            assertThat(OrderStatus.PARTIALLY_RETURNED.canTransitionTo(OrderStatus.RETURNED)).isTrue();
        }

        @Test
        @DisplayName("CANCEL_REQUESTED에서 CANCELLED로 전이할 수 있다")
        void cancelRequested_canTransitionTo_cancelled() {
            assertThat(OrderStatus.CANCEL_REQUESTED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("CANCEL_REQUESTED에서 CANCELLED 외 다른 상태로 전이할 수 없다")
        void cancelRequested_cannotTransitionTo_otherStatuses() {
            for (OrderStatus target : OrderStatus.values()) {
                if (target == OrderStatus.CANCELLED) {
                    continue;
                }
                assertThat(OrderStatus.CANCEL_REQUESTED.canTransitionTo(target))
                        .as("CANCEL_REQUESTED → %s는 불가해야 한다", target)
                        .isFalse();
            }
        }

        @Test
        @DisplayName("FAILED에서 어떤 상태로도 전이할 수 없다")
        void failed_cannotTransitionToAny() {
            for (OrderStatus target : OrderStatus.values()) {
                assertThat(OrderStatus.FAILED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("CANCELLED에서 어떤 상태로도 전이할 수 없다")
        void cancelled_cannotTransitionToAny() {
            for (OrderStatus target : OrderStatus.values()) {
                assertThat(OrderStatus.CANCELLED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("CONFIRMED에서 어떤 상태로도 전이할 수 없다")
        void confirmed_cannotTransitionToAny() {
            for (OrderStatus target : OrderStatus.values()) {
                assertThat(OrderStatus.CONFIRMED.canTransitionTo(target)).isFalse();
            }
        }

        @Test
        @DisplayName("RETURNED에서 어떤 상태로도 전이할 수 없다")
        void returned_cannotTransitionToAny() {
            for (OrderStatus target : OrderStatus.values()) {
                assertThat(OrderStatus.RETURNED.canTransitionTo(target)).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("최종 상태 검증 (isTerminal)")
    class IsTerminalTest {

        @Test
        @DisplayName("FAILED는 최종 상태이다")
        void failed_isTerminal() {
            assertThat(OrderStatus.FAILED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("CANCELLED는 최종 상태이다")
        void cancelled_isTerminal() {
            assertThat(OrderStatus.CANCELLED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("CONFIRMED는 최종 상태이다")
        void confirmed_isTerminal() {
            assertThat(OrderStatus.CONFIRMED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("RETURNED는 최종 상태이다")
        void returned_isTerminal() {
            assertThat(OrderStatus.RETURNED.isTerminal()).isTrue();
        }

        @Test
        @DisplayName("CANCEL_REQUESTED는 최종 상태가 아니다")
        void cancelRequested_isNotTerminal() {
            assertThat(OrderStatus.CANCEL_REQUESTED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("PAYMENT_PENDING는 최종 상태가 아니다")
        void paymentPending_isNotTerminal() {
            assertThat(OrderStatus.PAYMENT_PENDING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("PAID는 최종 상태가 아니다")
        void paid_isNotTerminal() {
            assertThat(OrderStatus.PAID.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("PREPARING는 최종 상태가 아니다")
        void preparing_isNotTerminal() {
            assertThat(OrderStatus.PREPARING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("SHIPPING는 최종 상태가 아니다")
        void shipping_isNotTerminal() {
            assertThat(OrderStatus.SHIPPING.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("DELIVERED는 최종 상태가 아니다")
        void delivered_isNotTerminal() {
            assertThat(OrderStatus.DELIVERED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("PARTIALLY_CONFIRMED는 최종 상태가 아니다")
        void partiallyConfirmed_isNotTerminal() {
            assertThat(OrderStatus.PARTIALLY_CONFIRMED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("RETURN_REQUESTED는 최종 상태가 아니다")
        void returnRequested_isNotTerminal() {
            assertThat(OrderStatus.RETURN_REQUESTED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("RETURN_IN_PROGRESS는 최종 상태가 아니다")
        void returnInProgress_isNotTerminal() {
            assertThat(OrderStatus.RETURN_IN_PROGRESS.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("PARTIALLY_RETURNED는 최종 상태가 아니다")
        void partiallyReturned_isNotTerminal() {
            assertThat(OrderStatus.PARTIALLY_RETURNED.isTerminal()).isFalse();
        }
    }

    @Nested
    @DisplayName("취소 요청 상태 검증 (isCancelRequested)")
    class IsCancelRequestedTest {

        @Test
        @DisplayName("CANCEL_REQUESTED는 취소 요청 상태이다")
        void cancelRequested_isCancelRequested() {
            assertThat(OrderStatus.CANCEL_REQUESTED.isCancelRequested()).isTrue();
        }

        @Test
        @DisplayName("CANCELLED는 취소 요청 상태가 아니다")
        void cancelled_isNotCancelRequested() {
            assertThat(OrderStatus.CANCELLED.isCancelRequested()).isFalse();
        }

        @Test
        @DisplayName("PAID는 취소 요청 상태가 아니다")
        void paid_isNotCancelRequested() {
            assertThat(OrderStatus.PAID.isCancelRequested()).isFalse();
        }
    }

    @Nested
    @DisplayName("배송 완료 상태 검증 (isDelivered)")
    class IsDeliveredTest {

        @Test
        @DisplayName("DELIVERED는 배송 완료 상태이다")
        void delivered_isDelivered() {
            assertThat(OrderStatus.DELIVERED.isDelivered()).isTrue();
        }

        @Test
        @DisplayName("SHIPPING는 배송 완료 상태가 아니다")
        void shipping_isNotDelivered() {
            assertThat(OrderStatus.SHIPPING.isDelivered()).isFalse();
        }

        @Test
        @DisplayName("CONFIRMED는 배송 완료 상태가 아니다")
        void confirmed_isNotDelivered() {
            assertThat(OrderStatus.CONFIRMED.isDelivered()).isFalse();
        }
    }
}
