package com.personal.marketnote.commerce.adapter.in.web.order.converter;

import com.personal.marketnote.commerce.domain.order.OrderStatusFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringToOrderStatusFilterConverterTest {

    private final StringToOrderStatusFilterConverter converter = new StringToOrderStatusFilterConverter();

    @Test
    @DisplayName("현재 enum 값 CANCEL_RETURN을 정상 변환한다")
    void converts_currentValue_cancelReturn() {
        OrderStatusFilter result = converter.convert("CANCEL_RETURN");

        assertThat(result).isEqualTo(OrderStatusFilter.CANCEL_RETURN);
    }

    @Test
    @DisplayName("레거시 값 CANCEL_EXCHANGE_REFUND를 CANCEL_RETURN으로 변환한다")
    void converts_legacyValue_cancelExchangeRefund_to_cancelReturn() {
        OrderStatusFilter result = converter.convert("CANCEL_EXCHANGE_REFUND");

        assertThat(result).isEqualTo(OrderStatusFilter.CANCEL_RETURN);
    }

    @Test
    @DisplayName("현재 enum 값 SHIPPING을 정상 변환한다")
    void converts_currentValue_shipping() {
        OrderStatusFilter result = converter.convert("SHIPPING");

        assertThat(result).isEqualTo(OrderStatusFilter.SHIPPING);
    }

    @Test
    @DisplayName("현재 enum 값 DELIVERED를 정상 변환한다")
    void converts_currentValue_delivered() {
        OrderStatusFilter result = converter.convert("DELIVERED");

        assertThat(result).isEqualTo(OrderStatusFilter.DELIVERED);
    }

    @Test
    @DisplayName("현재 enum 값 CONFIRMED를 정상 변환한다")
    void converts_currentValue_confirmed() {
        OrderStatusFilter result = converter.convert("CONFIRMED");

        assertThat(result).isEqualTo(OrderStatusFilter.CONFIRMED);
    }

    @Test
    @DisplayName("현재 enum 값 ALL을 정상 변환한다")
    void converts_currentValue_all() {
        OrderStatusFilter result = converter.convert("ALL");

        assertThat(result).isEqualTo(OrderStatusFilter.ALL);
    }

    @Test
    @DisplayName("존재하지 않는 값은 IllegalArgumentException을 발생시킨다")
    void throws_exception_for_unknownValue() {
        assertThatThrownBy(() -> converter.convert("INVALID_VALUE"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
