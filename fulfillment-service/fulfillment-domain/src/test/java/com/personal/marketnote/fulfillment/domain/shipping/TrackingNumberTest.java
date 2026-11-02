package com.personal.marketnote.fulfillment.domain.shipping;

import com.personal.marketnote.fulfillment.domain.shipping.exception.TrackingNumberNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrackingNumberTest {

    @Test
    @DisplayName("유효한 운송장번호로 생성 시 정상 생성된다")
    void createTrackingNumberWithValidValue() {
        TrackingNumber trackingNumber = TrackingNumber.of("1234567890");

        assertThat(trackingNumber.getValue()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("빈 문자열로 생성 시 TrackingNumberNoValueException 발생한다")
    void throwsExceptionWhenTrackingNumberIsEmpty() {
        assertThatThrownBy(() -> TrackingNumber.of(""))
                .isInstanceOf(TrackingNumberNoValueException.class);
    }

    @Test
    @DisplayName("공백만 있는 문자열로 생성 시 TrackingNumberNoValueException 발생한다")
    void throwsExceptionWhenTrackingNumberIsBlank() {
        assertThatThrownBy(() -> TrackingNumber.of("   "))
                .isInstanceOf(TrackingNumberNoValueException.class);
    }

    @Test
    @DisplayName("null로 생성 시 TrackingNumberNoValueException 발생한다")
    void throwsExceptionWhenTrackingNumberIsNull() {
        assertThatThrownBy(() -> TrackingNumber.of(null))
                .isInstanceOf(TrackingNumberNoValueException.class);
    }

    @Test
    @DisplayName("getValue()는 생성 시 전달한 값을 반환한다")
    void getValueReturnsOriginalValue() {
        String input = "ABC123456";

        TrackingNumber trackingNumber = TrackingNumber.of(input);

        assertThat(trackingNumber.getValue()).isEqualTo(input);
    }
}
