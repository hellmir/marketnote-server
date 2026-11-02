package com.personal.marketnote.commerce.domain.returnshipping;

import com.personal.marketnote.common.domain.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReturnShippingFeeCalculator 테스트")
class ReturnShippingFeeCalculatorTest {

    private static final Money ONE_WAY_SHIPPING_FEE = Money.of(3000L);
    private static final Money ROUND_TRIP_SHIPPING_FEE = Money.of(6000L);
    private static final Money FREE_SHIPPING_THRESHOLD = Money.of(30000L);

    @Nested
    @DisplayName("판매자 귀책")
    class SellerFaultTest {

        @Test
        @DisplayName("case5: 판매자 귀책이면 반품 택배비는 0원이다")
        void shouldReturnZeroWhenSellerFault() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.SELLER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.FULL_RETURN).remainingAmount(Money.zero()).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(Money.zero()).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(Money.zero());
        }
    }

    @Nested
    @DisplayName("고객 귀책 + 초기 유료 배송")
    class BuyerFaultPaidShippingTest {

        @Test
        @DisplayName("case2: 고객 귀책 + 초기 유료 배송이면 편도 배송비(3,000원)를 반환한다")
        void shouldReturnOneWayFeeWhenBuyerFaultAndPaidShipping() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.PAID_SHIPPING).returnType(ReturnType.FULL_RETURN).remainingAmount(Money.zero()).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(Money.zero()).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ONE_WAY_SHIPPING_FEE);
        }
    }

    @Nested
    @DisplayName("고객 귀책 + 초기 무료 배송 + 전체 반품")
    class BuyerFaultFreeShippingFullReturnTest {

        @Test
        @DisplayName("case1: 고객 귀책 + 초기 무료 배송 + 전체 반품이면 왕복 배송비(6,000원)를 반환한다")
        void shouldReturnRoundTripFeeWhenBuyerFaultFreeShippingFullReturn() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.FULL_RETURN).remainingAmount(Money.zero()).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(Money.zero()).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ROUND_TRIP_SHIPPING_FEE);
        }
    }

    @Nested
    @DisplayName("고객 귀책 + 초기 무료 배송 + 부분 반품")
    class BuyerFaultFreeShippingPartialReturnTest {

        @Test
        @DisplayName("case3: 부분 반품 후 잔여 금액이 무료 배송 기준 미달이면 왕복 배송비(6,000원)를 반환한다")
        void shouldReturnRoundTripFeeWhenRemainingBelowThreshold() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.PARTIAL_RETURN).remainingAmount(Money.of(29999L)).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(Money.zero()).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ROUND_TRIP_SHIPPING_FEE);
        }

        @Test
        @DisplayName("case4: 부분 반품 후 잔여 금액이 무료 배송 기준 유지이면 편도 배송비(3,000원)를 반환한다")
        void shouldReturnOneWayFeeWhenRemainingMeetsThreshold() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.PARTIAL_RETURN).remainingAmount(Money.of(35000L)).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(Money.zero()).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ONE_WAY_SHIPPING_FEE);
        }

        @Test
        @DisplayName("부분 반품 후 잔여 금액이 무료 배송 기준과 정확히 같으면 편도 배송비를 반환한다")
        void shouldReturnOneWayFeeWhenRemainingEqualsThreshold() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.PARTIAL_RETURN).remainingAmount(FREE_SHIPPING_THRESHOLD).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(Money.zero()).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(ONE_WAY_SHIPPING_FEE);
        }
    }

    @Nested
    @DisplayName("제주/도서산간 추가 배송비")
    class RegionSurchargeTest {
        private static final Money JEJU_SURCHARGE = Money.of(3000L);
        private static final Money ISLAND_SURCHARGE = Money.of(5000L);

        @Test
        @DisplayName("고객 귀책 + 유료 배송 + 제주 → 편도 배송비 + 제주 추가 배송비 1회")
        void shouldAddJejuSurchargeForPaidShippingOneWay() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.PAID_SHIPPING).returnType(ReturnType.FULL_RETURN).remainingAmount(Money.zero()).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(JEJU_SURCHARGE).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(Money.of(6000L));
        }

        @Test
        @DisplayName("고객 귀책 + 무료 배송 + 전체 반품 + 도서산간 → 왕복 배송비 + 도서산간 추가 배송비 2회")
        void shouldAddIslandSurchargeForRoundTrip() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.FULL_RETURN).remainingAmount(Money.zero()).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(ISLAND_SURCHARGE).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(Money.of(16000L));
        }

        @Test
        @DisplayName("고객 귀책 + 무료 배송 + 기준 미달 + 제주 → 왕복 배송비 + 제주 추가 배송비 2회")
        void shouldAddJejuSurchargeForBelowThresholdRoundTrip() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.PARTIAL_RETURN).remainingAmount(Money.of(20000L)).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(JEJU_SURCHARGE).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(Money.of(12000L));
        }

        @Test
        @DisplayName("판매자 귀책 + 제주 → 추가 배송비 포함 전액 면제 (0원)")
        void shouldReturnZeroForSellerFaultWithSurcharge() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.SELLER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.FULL_RETURN).remainingAmount(Money.zero()).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(JEJU_SURCHARGE).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("고객 귀책 + 무료 배송 + 기준 유지 + 도서산간 → 편도 배송비 + 도서산간 추가 배송비 1회")
        void shouldAddIslandSurchargeForAboveThresholdOneWay() {
            ReturnShippingFeeContext context = ReturnShippingFeeContext.from(ReturnShippingFeeContextCreateState.builder().faultType(FaultType.BUYER).initialShippingType(InitialShippingType.FREE_SHIPPING).returnType(ReturnType.PARTIAL_RETURN).remainingAmount(Money.of(35000L)).freeShippingThreshold(FREE_SHIPPING_THRESHOLD).shippingFee(ONE_WAY_SHIPPING_FEE).regionSurcharge(ISLAND_SURCHARGE).build());

            Money result = ReturnShippingFeeCalculator.calculate(context);

            assertThat(result).isEqualTo(Money.of(8000L));
        }
    }
}
