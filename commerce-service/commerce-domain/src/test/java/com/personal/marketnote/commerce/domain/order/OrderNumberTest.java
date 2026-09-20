package com.personal.marketnote.commerce.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderNumberTest {

    @Test
    @DisplayName("유효한 주문번호 문자열로 OrderNumber를 생성한다")
    void shouldCreateWithValidValue() {
        OrderNumber orderNumber = OrderNumber.of("20260413120000A1B2C3D");

        assertThat(orderNumber.getValue()).isEqualTo("20260413120000A1B2C3D");
    }

    @Test
    @DisplayName("generate 호출 시 non-blank 주문번호를 반환한다")
    void shouldGenerateNonBlankOrderNumber() {
        OrderNumber orderNumber = OrderNumber.generate();

        assertThat(orderNumber.getValue()).isNotBlank();
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 OrderNumberNoValueException을 던진다")
    void shouldThrowWhenValueIsBlank() {
        assertThatThrownBy(() -> OrderNumber.of(""))
                .isInstanceOf(OrderNumberNoValueException.class);
    }

    @Test
    @DisplayName("공백만 있는 문자열로 생성 시 OrderNumberNoValueException을 던진다")
    void shouldThrowWhenValueIsWhitespaceOnly() {
        assertThatThrownBy(() -> OrderNumber.of("   "))
                .isInstanceOf(OrderNumberNoValueException.class);
    }

    @Test
    @DisplayName("null로 생성 시 OrderNumberNoValueException을 던진다")
    void shouldThrowWhenValueIsNull() {
        assertThatThrownBy(() -> OrderNumber.of(null))
                .isInstanceOf(OrderNumberNoValueException.class);
    }

    @Test
    @DisplayName("같은 값의 OrderNumber는 equals가 true다")
    void shouldBeEqualWhenSameValue() {
        OrderNumber a = OrderNumber.of("20260413120000A1B2C3D");
        OrderNumber b = OrderNumber.of("20260413120000A1B2C3D");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
