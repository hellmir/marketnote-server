package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.personal.marketnote.common.domain.money.Money;

import static org.assertj.core.api.Assertions.assertThat;

class OrderAmountTest {

    @Nested
    @DisplayName("OrderAmount 생성 (of)")
    class OfTest {

        @Test
        @DisplayName("모든 금액 필드가 정상적으로 설정된다")
        void shouldCreateWithAllFields() {
            // given & when
            OrderAmount orderAmount = OrderAmount.from(OrderAmountCreateState.builder().totalAmount(50000L).paidAmount(45000L).couponAmount(3000L).pointAmount(2000L).shippingFee(3000L).build());

            // then
            assertThat(orderAmount.getTotalAmount()).isEqualTo(Money.of(50000L));
            assertThat(orderAmount.getPaidAmount()).isEqualTo(Money.of(45000L));
            assertThat(orderAmount.getCouponAmount()).isEqualTo(Money.of(3000L));
            assertThat(orderAmount.getPointAmount()).isEqualTo(Money.of(2000L));
            assertThat(orderAmount.getShippingFee()).isEqualTo(Money.of(3000L));
        }

        @Test
        @DisplayName("할인 금액과 배송비가 null이어도 정상 생성되고 paidAmount는 Money.zero()로 초기화된다")
        void shouldCreateWithNullDiscountsAndShippingFee() {
            // given & when
            OrderAmount orderAmount = OrderAmount.from(OrderAmountCreateState.builder().totalAmount(50000L).paidAmount(null).couponAmount(null).pointAmount(null).shippingFee(null).build());

            // then
            assertThat(orderAmount.getTotalAmount()).isEqualTo(Money.of(50000L));
            assertThat(orderAmount.getPaidAmount()).isEqualTo(Money.zero());
            assertThat(orderAmount.getCouponAmount()).isEqualTo(Money.zero());
            assertThat(orderAmount.getPointAmount()).isEqualTo(Money.zero());
            assertThat(orderAmount.getShippingFee()).isEqualTo(Money.zero());
        }
    }

    @Nested
    @DisplayName("OrderAmount 생성 (from CreateState)")
    class FromCreateStateTest {

        @Test
        @DisplayName("CreateState에 paidAmount가 없으면 Money.zero()로 초기화된다")
        void shouldInitializePaidAmountAsZeroWhenCreateStateHasNoPaidAmount() {
            OrderAmountCreateState state = OrderAmountCreateState.builder()
                    .totalAmount(50000L)
                    .couponAmount(3000L)
                    .pointAmount(2000L)
                    .shippingFee(3000L)
                    .build();

            OrderAmount orderAmount = OrderAmount.from(state);

            assertThat(orderAmount.getTotalAmount()).isEqualTo(Money.of(50000L));
            assertThat(orderAmount.getPaidAmount()).isEqualTo(Money.zero());
            assertThat(orderAmount.getCouponAmount()).isEqualTo(Money.of(3000L));
            assertThat(orderAmount.getPointAmount()).isEqualTo(Money.of(2000L));
            assertThat(orderAmount.getShippingFee()).isEqualTo(Money.of(3000L));
        }

        @Test
        @DisplayName("CreateState에 paidAmount가 있으면 Money로 변환되어 설정된다")
        void shouldConvertPaidAmountToMoneyWhenCreateStateHasPaidAmount() {
            OrderAmountCreateState state = OrderAmountCreateState.builder()
                    .totalAmount(50000L)
                    .paidAmount(45000L)
                    .couponAmount(3000L)
                    .pointAmount(2000L)
                    .shippingFee(3000L)
                    .build();

            OrderAmount orderAmount = OrderAmount.from(state);

            assertThat(orderAmount.getPaidAmount()).isEqualTo(Money.of(45000L));
        }
    }

    @Nested
    @DisplayName("OrderAmount 생성 (from SnapshotState)")
    class FromSnapshotStateTest {

        @Test
        @DisplayName("SnapshotState에 paidAmount가 null이면 Money.zero()로 복원된다")
        void shouldRestorePaidAmountAsZeroWhenSnapshotStateHasNullPaidAmount() {
            OrderAmountSnapshotState state = OrderAmountSnapshotState.builder()
                    .totalAmount(50000L)
                    .paidAmount(null)
                    .couponAmount(3000L)
                    .pointAmount(2000L)
                    .shippingFee(3000L)
                    .build();

            OrderAmount orderAmount = OrderAmount.from(state);

            assertThat(orderAmount.getPaidAmount()).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("SnapshotState에 paidAmount가 있으면 Money로 변환되어 복원된다")
        void shouldRestorePaidAmountAsMoneyWhenSnapshotStateHasPaidAmount() {
            OrderAmountSnapshotState state = OrderAmountSnapshotState.builder()
                    .totalAmount(50000L)
                    .paidAmount(45000L)
                    .couponAmount(3000L)
                    .pointAmount(2000L)
                    .shippingFee(3000L)
                    .build();

            OrderAmount orderAmount = OrderAmount.from(state);

            assertThat(orderAmount.getPaidAmount()).isEqualTo(Money.of(45000L));
        }
    }

    @Nested
    @DisplayName("withPaidAmount 불변 업데이트")
    class WithPaidAmountTest {

        @Test
        @DisplayName("withPaidAmount는 원본 OrderAmount를 변경하지 않고 paidAmount만 변경된 새 인스턴스를 반환한다")
        void shouldReturnNewInstanceWithUpdatedPaidAmountAndKeepOriginalUnchanged() {
            OrderAmount original = OrderAmount.from(OrderAmountCreateState.builder()
                    .totalAmount(50000L)
                    .couponAmount(3000L)
                    .pointAmount(2000L)
                    .shippingFee(3000L)
                    .build()
            );

            OrderAmount updated = original.withPaidAmount(Money.of(45000L));

            assertThat(original.getPaidAmount()).isEqualTo(Money.zero());
            assertThat(updated.getPaidAmount()).isEqualTo(Money.of(45000L));
            assertThat(updated.getTotalAmount()).isEqualTo(original.getTotalAmount());
            assertThat(updated.getCouponAmount()).isEqualTo(original.getCouponAmount());
            assertThat(updated.getPointAmount()).isEqualTo(original.getPointAmount());
            assertThat(updated.getShippingFee()).isEqualTo(original.getShippingFee());
        }
    }
}
