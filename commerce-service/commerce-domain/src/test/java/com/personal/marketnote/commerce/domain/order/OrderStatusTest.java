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
        @DisplayName("CANCELLED는 구매자가 변경할 수 없는 상태이다")
        void cancelled_isNotBuyerAllowed() {
            assertThat(OrderStatus.CANCELLED.isBuyerAllowed()).isFalse();
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
        @DisplayName("RETURN_RECALLING는 구매자가 변경할 수 없는 상태이다")
        void returnRecalling_isNotBuyerAllowed() {
            assertThat(OrderStatus.RETURN_RECALLING.isBuyerAllowed()).isFalse();
        }

        @Test
        @DisplayName("RETURN_SHIPPING는 구매자가 변경할 수 없는 상태이다")
        void returnShipping_isNotBuyerAllowed() {
            assertThat(OrderStatus.RETURN_SHIPPING.isBuyerAllowed()).isFalse();
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
}
