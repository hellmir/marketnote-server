package com.personal.marketnote.fulfillment.domain.shipping;

import com.personal.marketnote.fulfillment.domain.shipping.exception.CarrierCodeNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarrierCodeTest {

    @Test
    @DisplayName("유효한 택배사 코드로 생성 시 정상 생성된다")
    void createCarrierCodeWithValidValue() {
        CarrierCode carrierCode = CarrierCode.of("CJ");

        assertThat(carrierCode.getValue()).isEqualTo("CJ");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 CarrierCodeNoValueException 발생한다")
    void throwsExceptionWhenCarrierCodeIsEmpty() {
        assertThatThrownBy(() -> CarrierCode.of(""))
                .isInstanceOf(CarrierCodeNoValueException.class);
    }

    @Test
    @DisplayName("공백만 있는 문자열로 생성 시 CarrierCodeNoValueException 발생한다")
    void throwsExceptionWhenCarrierCodeIsBlank() {
        assertThatThrownBy(() -> CarrierCode.of("   "))
                .isInstanceOf(CarrierCodeNoValueException.class);
    }

    @Test
    @DisplayName("null로 생성 시 CarrierCodeNoValueException 발생한다")
    void throwsExceptionWhenCarrierCodeIsNull() {
        assertThatThrownBy(() -> CarrierCode.of(null))
                .isInstanceOf(CarrierCodeNoValueException.class);
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환한다")
    void getValueReturnsOriginalValue() {
        String input = "HANJIN";

        CarrierCode carrierCode = CarrierCode.of(input);

        assertThat(carrierCode.getValue()).isEqualTo(input);
    }
}
