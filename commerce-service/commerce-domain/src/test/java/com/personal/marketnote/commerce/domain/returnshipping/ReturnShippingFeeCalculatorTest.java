package com.personal.marketnote.commerce.domain.returnshipping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReturnShippingFeeCalculator 테스트")
class ReturnShippingFeeCalculatorTest {

    private static final long ONE_WAY_SHIPPING_FEE = 3000L;
    private static final long ROUND_TRIP_SHIPPING_FEE = 6000L;
    private static final long FREE_SHIPPING_THRESHOLD = 30000L;

    @Nested
    @DisplayName("판매자 귀책")
    class SellerFaultTest {

        @Test
        @DisplayName("case5: 판매자 귀책이면 반품 택배비는 0원이다")
        void shouldReturnZeroWhenSellerFault() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.of(
                    FaultType.SELLER,
                    InitialShippingType.FREE_SHIPPING,
                    ReturnType.FULL_RETURN,
                    0L,
                    FREE_SHIPPING_THRESHOLD,
                    ONE_WAY_SHIPPING_FEE
            );

            long result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isZero();
        }
    }

    @Nested
    @DisplayName("고객 귀책 + 초기 유료 배송")
    class BuyerFaultPaidShippingTest {

        @Test
        @DisplayName("case2: 고객 귀책 + 초기 유료 배송이면 편도 배송비(3,000원)를 반환한다")
        void shouldReturnOneWayFeeWhenBuyerFaultAndPaidShipping() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.of(
                    FaultType.BUYER,
                    InitialShippingType.PAID_SHIPPING,
                    ReturnType.FULL_RETURN,
                    0L,
                    FREE_SHIPPING_THRESHOLD,
                    ONE_WAY_SHIPPING_FEE
            );

            long result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ONE_WAY_SHIPPING_FEE);
        }
    }

    @Nested
    @DisplayName("고객 귀책 + 초기 무료 배송 + 전체 반품")
    class BuyerFaultFreeShippingFullReturnTest {

        @Test
        @DisplayName("case1: 고객 귀책 + 초기 무료 배송 + 전체 반품이면 왕복 배송비(6,000원)를 반환한다")
        void shouldReturnRoundTripFeeWhenBuyerFaultFreeShippingFullReturn() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.of(
                    FaultType.BUYER,
                    InitialShippingType.FREE_SHIPPING,
                    ReturnType.FULL_RETURN,
                    0L,
                    FREE_SHIPPING_THRESHOLD,
                    ONE_WAY_SHIPPING_FEE
            );

            long result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ROUND_TRIP_SHIPPING_FEE);
        }
    }

    @Nested
    @DisplayName("고객 귀책 + 초기 무료 배송 + 부분 반품")
    class BuyerFaultFreeShippingPartialReturnTest {

        @Test
        @DisplayName("case3: 부분 반품 후 잔여 금액이 무료 배송 기준 미달이면 왕복 배송비(6,000원)를 반환한다")
        void shouldReturnRoundTripFeeWhenRemainingBelowThreshold() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.of(
                    FaultType.BUYER,
                    InitialShippingType.FREE_SHIPPING,
                    ReturnType.PARTIAL_RETURN,
                    29999L,
                    FREE_SHIPPING_THRESHOLD,
                    ONE_WAY_SHIPPING_FEE
            );

            long result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ROUND_TRIP_SHIPPING_FEE);
        }

        @Test
        @DisplayName("case4: 부분 반품 후 잔여 금액이 무료 배송 기준 유지이면 편도 배송비(3,000원)를 반환한다")
        void shouldReturnOneWayFeeWhenRemainingMeetsThreshold() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.of(
                    FaultType.BUYER,
                    InitialShippingType.FREE_SHIPPING,
                    ReturnType.PARTIAL_RETURN,
                    35000L,
                    FREE_SHIPPING_THRESHOLD,
                    ONE_WAY_SHIPPING_FEE
            );

            long result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ONE_WAY_SHIPPING_FEE);
        }

        @Test
        @DisplayName("부분 반품 후 잔여 금액이 무료 배송 기준과 정확히 같으면 편도 배송비를 반환한다")
        void shouldReturnOneWayFeeWhenRemainingEqualsThreshold() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.of(
                    FaultType.BUYER,
                    InitialShippingType.FREE_SHIPPING,
                    ReturnType.PARTIAL_RETURN,
                    FREE_SHIPPING_THRESHOLD,
                    FREE_SHIPPING_THRESHOLD,
                    ONE_WAY_SHIPPING_FEE
            );

            long result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ONE_WAY_SHIPPING_FEE);
        }
    }
}
