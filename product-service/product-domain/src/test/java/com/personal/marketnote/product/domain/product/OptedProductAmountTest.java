package com.personal.marketnote.product.domain.product;

import com.personal.marketnote.common.domain.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OptedProductAmount 도메인 테스트")
class OptedProductAmountTest {

    @Nested
    @DisplayName("초기 상태")
    class InitialState {

        @Test
        @DisplayName("초기 상태에서 totalOptionPrice와 totalOptionPoint는 0원이다")
        void shouldHaveZeroTotalOptionPriceAndPointInitially() {
            // given
            OptedProductAmount amount = new OptedProductAmount();

            // when & then
            assertThat(amount.getTotalOptionPrice()).isEqualTo(Money.zero());
            assertThat(amount.getTotalOptionPoint()).isEqualTo(Money.zero());
        }
    }

    @Nested
    @DisplayName("addAmount()")
    class AddAmount {

        @Test
        @DisplayName("addAmount 호출 시 가격과 포인트가 누적된다")
        void shouldAccumulatePriceAndPointWhenAddAmountCalled() {
            // given
            OptedProductAmount amount = new OptedProductAmount();

            // when
            amount.addAmount(Money.of(5000L), Money.of(100L));

            // then
            assertThat(amount.getTotalOptionPrice()).isEqualTo(Money.of(5000L));
            assertThat(amount.getTotalOptionPoint()).isEqualTo(Money.of(100L));
        }

        @Test
        @DisplayName("addAmount 여러 번 호출 시 가격과 포인트가 정확히 누적된다")
        void shouldAccumulatePriceAndPointCorrectlyWhenAddAmountCalledMultipleTimes() {
            // given
            OptedProductAmount amount = new OptedProductAmount();

            // when
            amount.addAmount(Money.of(3000L), Money.of(50L));
            amount.addAmount(Money.of(2000L), Money.of(30L));
            amount.addAmount(Money.of(1000L), Money.of(20L));

            // then
            assertThat(amount.getTotalOptionPrice()).isEqualTo(Money.of(6000L));
            assertThat(amount.getTotalOptionPoint()).isEqualTo(Money.of(100L));
        }
    }
}
