package com.personal.marketnote.commerce.domain.shipping;

import com.personal.marketnote.common.domain.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShippingFeeCalculator 테스트")
class ShippingFeeCalculatorTest {

    @Test
    @DisplayName("판매자 금액이 무료배송 기준 미만이면 배송비를 반환한다")
    void shouldReturnShippingFeeWhenBelowThreshold() {
        // given
        ShippingFeeContext context = ShippingFeeContext.of(Money.of(29000L), Money.of(3000L), Money.of(30000L));

        // when
        Money baseFee = ShippingFeeCalculator.calculateBaseFee(context);

        // then
        assertThat(baseFee).isEqualTo(Money.of(3000L));
    }

    @Test
    @DisplayName("판매자 금액이 무료배송 기준 이상이면 0을 반환한다")
    void shouldReturnZeroWhenAboveThreshold() {
        // given
        ShippingFeeContext context = ShippingFeeContext.of(Money.of(50000L), Money.of(3000L), Money.of(30000L));

        // when
        Money baseFee = ShippingFeeCalculator.calculateBaseFee(context);

        // then
        assertThat(baseFee).isEqualTo(Money.zero());
    }

    @Test
    @DisplayName("판매자 금액이 무료배송 기준과 동일하면 0을 반환한다")
    void shouldReturnZeroWhenExactlyAtThreshold() {
        // given
        ShippingFeeContext context = ShippingFeeContext.of(Money.of(30000L), Money.of(3000L), Money.of(30000L));

        // when
        Money baseFee = ShippingFeeCalculator.calculateBaseFee(context);

        // then
        assertThat(baseFee).isEqualTo(Money.zero());
    }
}
