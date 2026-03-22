package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderAmountTest {

    @Nested
    @DisplayName("OrderAmount 생성 (of)")
    class OfTest {

        @Test
        @DisplayName("모든 금액 필드가 정상적으로 설정된다")
        void shouldCreateWithAllFields() {
            // given & when
            OrderAmount orderAmount = OrderAmount.of(50000L, 45000L, 3000L, 2000L, 3000L);

            // then
            assertThat(orderAmount.getTotalAmount()).isEqualTo(50000L);
            assertThat(orderAmount.getPaidAmount()).isEqualTo(45000L);
            assertThat(orderAmount.getCouponAmount()).isEqualTo(3000L);
            assertThat(orderAmount.getPointAmount()).isEqualTo(2000L);
            assertThat(orderAmount.getShippingFee()).isEqualTo(3000L);
        }

        @Test
        @DisplayName("할인 금액과 배송비가 null이어도 정상 생성된다")
        void shouldCreateWithNullDiscountsAndShippingFee() {
            // given & when
            OrderAmount orderAmount = OrderAmount.of(50000L, null, null, null, null);

            // then
            assertThat(orderAmount.getTotalAmount()).isEqualTo(50000L);
            assertThat(orderAmount.getPaidAmount()).isNull();
            assertThat(orderAmount.getCouponAmount()).isNull();
            assertThat(orderAmount.getPointAmount()).isNull();
            assertThat(orderAmount.getShippingFee()).isNull();
        }
    }
}
