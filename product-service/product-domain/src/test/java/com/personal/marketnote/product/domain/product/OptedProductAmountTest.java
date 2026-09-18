package com.personal.marketnote.product.domain.product;

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
        @DisplayName("초기 상태에서 totalOptionPrice와 totalOptionPoint는 0이다")
        void shouldHaveZeroTotalOptionPriceAndPointInitially() {
            // given
            OptedProductAmount amount = new OptedProductAmount();

            // when & then
            assertThat(amount.getTotalOptionPrice()).isZero();
            assertThat(amount.getTotalOptionPoint()).isZero();
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
            amount.addAmount(5000L, 100L);

            // then
            assertThat(amount.getTotalOptionPrice()).isEqualTo(5000L);
            assertThat(amount.getTotalOptionPoint()).isEqualTo(100L);
        }

        @Test
        @DisplayName("addAmount 여러 번 호출 시 가격과 포인트가 정확히 누적된다")
        void shouldAccumulatePriceAndPointCorrectlyWhenAddAmountCalledMultipleTimes() {
            // given
            OptedProductAmount amount = new OptedProductAmount();

            // when
            amount.addAmount(3000L, 50L);
            amount.addAmount(2000L, 30L);
            amount.addAmount(1000L, 20L);

            // then
            assertThat(amount.getTotalOptionPrice()).isEqualTo(6000L);
            assertThat(amount.getTotalOptionPoint()).isEqualTo(100L);
        }
    }
}
