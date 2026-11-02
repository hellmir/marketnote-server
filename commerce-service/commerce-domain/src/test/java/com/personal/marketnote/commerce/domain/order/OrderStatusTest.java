package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Nested
    @DisplayName("상태 전이 가능 여부 검증 (canTransitionTo)")
    class CanTransitionToTest {

        @Test
        @DisplayName("PAYMENT_PENDING에서 PAYMENT_EXECUTING로 전이할 수 있다")
        void paymentPending_canTransitionTo_paymentExecuting() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.PAYMENT_EXECUTING)).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 PAID로 직접 전이할 수 없다 (PAYMENT_EXECUTING 경유 필수)")
        void paymentPending_cannotTransitionTo_paid() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.PAID)).isFalse();
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 FAILED로 직접 전이할 수 없다 (PAYMENT_EXECUTING 경유 필수)")
        void paymentPending_cannotTransitionTo_failed() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.FAILED)).isFalse();
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 CANCELLED로 전이할 수 있다")
        void paymentPending_canTransitionTo_cancelled() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 PARTIALLY_CANCELLED로 전이할 수 있다")
        void paymentPending_canTransitionTo_partiallyCancelled() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.PARTIALLY_CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_PENDING에서 SHIPPING로 전이할 수 없다")
        void paymentPending_cannotTransitionTo_shipping() {
            assertThat(OrderStatus.PAYMENT_PENDING.canTransitionTo(OrderStatus.SHIPPING)).isFalse();
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING에서 PAID로 전이할 수 있다")
        void paymentExecuting_canTransitionTo_paid() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.canTransitionTo(OrderStatus.PAID)).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING에서 FAILED로 전이할 수 있다")
        void paymentExecuting_canTransitionTo_failed() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.canTransitionTo(OrderStatus.FAILED)).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING에서 CANCEL_REQUESTED로 전이할 수 없다 (결제 진행 중 취소 불가)")
        void paymentExecuting_cannotTransitionTo_cancelRequested() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.canTransitionTo(OrderStatus.CANCEL_REQUESTED)).isFalse();
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING에서 CANCELLED로 직접 전이할 수 없다 (CANCEL_REQUESTED 경유 필수)")
        void paymentExecuting_cannotTransitionTo_cancelled() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING에서 SHIPPING로 전이할 수 없다")
        void paymentExecuting_cannotTransitionTo_shipping() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.canTransitionTo(OrderStatus.SHIPPING)).isFalse();
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING에서 PAYMENT_PENDING로 전이할 수 없다")
        void paymentExecuting_cannotTransitionTo_paymentPending() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.canTransitionTo(OrderStatus.PAYMENT_PENDING)).isFalse();
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
        @DisplayName("PAID에서 CANCELLED로 전이할 수 있다 (부분 취소)")
        void paid_canTransitionTo_cancelled() {
            assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
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
        @DisplayName("PREPARING에서 CANCELLED로 전이할 수 있다 (부분 취소)")
        void preparing_canTransitionTo_cancelled() {
            assertThat(OrderStatus.PREPARING.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
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
        @DisplayName("PAID에서 PARTIALLY_CANCEL_REQUESTED로 전이할 수 있다")
        void paid_canTransitionTo_partiallyCancelRequested() {
            assertThat(OrderStatus.PAID.canTransitionTo(OrderStatus.PARTIALLY_CANCEL_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PREPARING에서 PARTIALLY_CANCEL_REQUESTED로 전이할 수 있다")
        void preparing_canTransitionTo_partiallyCancelRequested() {
            assertThat(OrderStatus.PREPARING.canTransitionTo(OrderStatus.PARTIALLY_CANCEL_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_CANCEL_REQUESTED에서 CANCELLED로 전이할 수 있다")
        void partiallyCancelRequested_canTransitionTo_cancelled() {
            assertThat(OrderStatus.PARTIALLY_CANCEL_REQUESTED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_CANCEL_REQUESTED에서 PARTIALLY_CANCELLED로 전이할 수 있다")
        void partiallyCancelRequested_canTransitionTo_partiallyCancelled() {
            assertThat(OrderStatus.PARTIALLY_CANCEL_REQUESTED.canTransitionTo(OrderStatus.PARTIALLY_CANCELLED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_CANCEL_REQUESTED에서 SHIPPING로 전이할 수 없다")
        void partiallyCancelRequested_cannotTransitionTo_shipping() {
            assertThat(OrderStatus.PARTIALLY_CANCEL_REQUESTED.canTransitionTo(OrderStatus.SHIPPING)).isFalse();
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
        @DisplayName("PAYMENT_EXECUTING는 최종 상태가 아니다")
        void paymentExecuting_isNotTerminal() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.isTerminal()).isFalse();
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

    @Nested
    @DisplayName("PARTIALLY_RETURN_REQUESTED 전이 검증")
    class PartiallyReturnRequestedTransitionTest {

        @Test
        @DisplayName("SHIPPING에서 PARTIALLY_RETURN_REQUESTED로 전이할 수 있다")
        void shipping_canTransitionTo_partiallyReturnRequested() {
            assertThat(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.PARTIALLY_RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("DELIVERED에서 PARTIALLY_RETURN_REQUESTED로 전이할 수 있다")
        void delivered_canTransitionTo_partiallyReturnRequested() {
            assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.PARTIALLY_RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_CONFIRMED에서 PARTIALLY_RETURN_REQUESTED로 전이할 수 있다")
        void partiallyConfirmed_canTransitionTo_partiallyReturnRequested() {
            assertThat(OrderStatus.PARTIALLY_CONFIRMED.canTransitionTo(OrderStatus.PARTIALLY_RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RETURNED에서 PARTIALLY_RETURN_REQUESTED로 전이할 수 있다")
        void partiallyReturned_canTransitionTo_partiallyReturnRequested() {
            assertThat(OrderStatus.PARTIALLY_RETURNED.canTransitionTo(OrderStatus.PARTIALLY_RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RETURN_REQUESTED에서 RETURN_IN_PROGRESS로 전이할 수 있다")
        void partiallyReturnRequested_canTransitionTo_returnInProgress() {
            assertThat(OrderStatus.PARTIALLY_RETURN_REQUESTED.canTransitionTo(OrderStatus.RETURN_IN_PROGRESS)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RETURN_REQUESTED에서 RETURN_REJECTED로 전이할 수 있다")
        void partiallyReturnRequested_canTransitionTo_returnRejected() {
            assertThat(OrderStatus.PARTIALLY_RETURN_REQUESTED.canTransitionTo(OrderStatus.RETURN_REJECTED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RETURN_REQUESTED에서 RETURN_REQUESTED로 전이할 수 있다")
        void partiallyReturnRequested_canTransitionTo_returnRequested() {
            assertThat(OrderStatus.PARTIALLY_RETURN_REQUESTED.canTransitionTo(OrderStatus.RETURN_REQUESTED)).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_RETURN_REQUESTED에서 CONFIRMED로 전이할 수 없다")
        void partiallyReturnRequested_cannotTransitionTo_confirmed() {
            assertThat(OrderStatus.PARTIALLY_RETURN_REQUESTED.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
        }

        @Test
        @DisplayName("PARTIALLY_RETURN_REQUESTED는 최종 상태가 아니다")
        void partiallyReturnRequested_isNotTerminal() {
            assertThat(OrderStatus.PARTIALLY_RETURN_REQUESTED.isTerminal()).isFalse();
        }

        @Test
        @DisplayName("isPartiallyReturnRequested는 PARTIALLY_RETURN_REQUESTED 상태에서 true를 반환한다")
        void isPartiallyReturnRequested_true() {
            assertThat(OrderStatus.PARTIALLY_RETURN_REQUESTED.isPartiallyReturnRequested()).isTrue();
        }

        @Test
        @DisplayName("isPartiallyReturnRequested는 RETURN_REQUESTED 상태에서 false를 반환한다")
        void isPartiallyReturnRequested_falseForReturnRequested() {
            assertThat(OrderStatus.RETURN_REQUESTED.isPartiallyReturnRequested()).isFalse();
        }

        @Test
        @DisplayName("isNotPartialChanged는 PARTIALLY_RETURN_REQUESTED 상태에서 false를 반환한다")
        void isNotPartialChanged_falseForPartiallyReturnRequested() {
            assertThat(OrderStatus.PARTIALLY_RETURN_REQUESTED.isNotPartialChanged()).isFalse();
        }

        @Test
        @DisplayName("getPartiallyReturnRequested는 PARTIALLY_RETURN_REQUESTED 상수를 반환한다")
        void getPartiallyReturnRequested_returnsConstant() {
            assertThat(OrderStatus.getPartiallyReturnRequested()).isEqualTo(OrderStatus.PARTIALLY_RETURN_REQUESTED);
        }
    }

    @Nested
    @DisplayName("결제 진행 중 상태 검증 (isExecuting)")
    class IsExecutingTest {

        @Test
        @DisplayName("PAYMENT_EXECUTING는 결제 진행 중 상태이다")
        void paymentExecuting_isExecuting() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.isExecuting()).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_PENDING은 결제 진행 중 상태가 아니다")
        void paymentPending_isNotExecuting() {
            assertThat(OrderStatus.PAYMENT_PENDING.isExecuting()).isFalse();
        }

        @Test
        @DisplayName("PAID는 결제 진행 중 상태가 아니다")
        void paid_isNotExecuting() {
            assertThat(OrderStatus.PAID.isExecuting()).isFalse();
        }

        @Test
        @DisplayName("FAILED는 결제 진행 중 상태가 아니다")
        void failed_isNotExecuting() {
            assertThat(OrderStatus.FAILED.isExecuting()).isFalse();
        }
    }

    @Nested
    @DisplayName("클라이언트 노출 상태 변환 (toClientVisible)")
    class ToClientVisibleTest {

        @Test
        @DisplayName("PAYMENT_EXECUTING은 클라이언트 대상으로 PAYMENT_PENDING으로 변환된다")
        void paymentExecuting_toClientVisible_returnsPaymentPending() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.toClientVisible()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }

        @Test
        @DisplayName("PAYMENT_PENDING은 변환 없이 자기 자신을 반환한다")
        void paymentPending_toClientVisible_returnsSelf() {
            assertThat(OrderStatus.PAYMENT_PENDING.toClientVisible()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        }

        @Test
        @DisplayName("PAID는 변환 없이 자기 자신을 반환한다")
        void paid_toClientVisible_returnsSelf() {
            assertThat(OrderStatus.PAID.toClientVisible()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        @DisplayName("FAILED는 변환 없이 자기 자신을 반환한다")
        void failed_toClientVisible_returnsSelf() {
            assertThat(OrderStatus.FAILED.toClientVisible()).isEqualTo(OrderStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("부분 취소 가능 여부 검증 (canPartiallyCancel)")
    class CanPartiallyCancelTest {

        @Test
        @DisplayName("PAYMENT_PENDING은 부분 취소 가능 상태이다")
        void paymentPending_canPartiallyCancel() {
            assertThat(OrderStatus.PAYMENT_PENDING.canPartiallyCancel()).isTrue();
        }

        @Test
        @DisplayName("PAID는 부분 취소 가능 상태이다")
        void paid_canPartiallyCancel() {
            assertThat(OrderStatus.PAID.canPartiallyCancel()).isTrue();
        }

        @Test
        @DisplayName("PREPARING은 부분 취소 가능 상태이다")
        void preparing_canPartiallyCancel() {
            assertThat(OrderStatus.PREPARING.canPartiallyCancel()).isTrue();
        }

        @Test
        @DisplayName("PARTIALLY_CANCELLED는 부분 취소 가능 상태이다")
        void partiallyCancelled_canPartiallyCancel() {
            assertThat(OrderStatus.PARTIALLY_CANCELLED.canPartiallyCancel()).isTrue();
        }

        @Test
        @DisplayName("PAYMENT_EXECUTING은 부분 취소 가능 상태가 아니다 (결제 진행 중 부분 취소 금지)")
        void paymentExecuting_cannotPartiallyCancel() {
            assertThat(OrderStatus.PAYMENT_EXECUTING.canPartiallyCancel()).isFalse();
        }

        @Test
        @DisplayName("SHIPPING은 부분 취소 가능 상태가 아니다")
        void shipping_cannotPartiallyCancel() {
            assertThat(OrderStatus.SHIPPING.canPartiallyCancel()).isFalse();
        }

        @Test
        @DisplayName("CANCELLED는 부분 취소 가능 상태가 아니다")
        void cancelled_cannotPartiallyCancel() {
            assertThat(OrderStatus.CANCELLED.canPartiallyCancel()).isFalse();
        }
    }
}
