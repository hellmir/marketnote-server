package com.personal.marketnote.commerce.domain.inventory;

import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InsufficientQuantityException;
import com.personal.marketnote.common.domain.exception.illegalargument.invalidvalue.InvalidQuantityException;
import com.personal.marketnote.common.domain.exception.illegalargument.novalue.QuantityNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    @Test
    @DisplayName("양의 정수 문자열로 Stock을 생성한다")
    void shouldCreateStockWithPositiveInteger() {
        Stock stock = Stock.of("100");

        assertThat(stock.getValue()).isEqualTo(100);
    }

    @Test
    @DisplayName("0으로 Stock을 생성한다")
    void shouldCreateStockWithZero() {
        Stock stock = Stock.of("0");

        assertThat(stock.getValue()).isEqualTo(0);
    }

    @Test
    @DisplayName("null이면 QuantityNoValueException을 던진다")
    void shouldThrowWhenStockIsNull() {
        assertThatThrownBy(() -> Stock.of(null))
                .isInstanceOf(QuantityNoValueException.class);
    }

    @Test
    @DisplayName("빈 문자열이면 QuantityNoValueException을 던진다")
    void shouldThrowWhenStockIsBlank() {
        assertThatThrownBy(() -> Stock.of(""))
                .isInstanceOf(QuantityNoValueException.class);
    }

    @Test
    @DisplayName("음수이면 InvalidQuantityException을 던진다")
    void shouldThrowWhenStockIsNegative() {
        assertThatThrownBy(() -> Stock.of("-1"))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("비정수 문자열이면 InvalidQuantityException을 던진다")
    void shouldThrowWhenStockIsNotInteger() {
        assertThatThrownBy(() -> Stock.of("abc"))
                .isInstanceOf(InvalidQuantityException.class);
    }

    @Test
    @DisplayName("reduce 시 재고가 충분하면 차감된 결과를 반환한다")
    void shouldReduceStockWhenSufficient() {
        Stock stock = Stock.of("100");

        Integer result = stock.reduce(30);

        assertThat(result).isEqualTo(70);
    }

    @Test
    @DisplayName("reduce 시 재고가 부족하면 InsufficientQuantityException을 던진다")
    void shouldThrowWhenReduceExceedsStock() {
        Stock stock = Stock.of("10");

        assertThatThrownBy(() -> stock.reduce(20))
                .isInstanceOf(InsufficientQuantityException.class);
    }

    @Test
    @DisplayName("increase 시 양수 수량이면 증가된 결과를 반환한다")
    void shouldIncreaseStockWithPositiveQuantity() {
        Stock stock = Stock.of("100");

        Integer result = stock.increase(50);

        assertThat(result).isEqualTo(150);
    }

    @Test
    @DisplayName("increase 시 0 이하 수량이면 InvalidQuantityException을 던진다")
    void shouldThrowWhenIncreaseWithZeroOrNegative() {
        Stock stock = Stock.of("100");

        assertThatThrownBy(() -> stock.increase(0))
                .isInstanceOf(InvalidQuantityException.class);
    }
}
