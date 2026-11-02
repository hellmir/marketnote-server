package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkCouponTypeTest {

    @ParameterizedTest
    @CsvSource({
            "TYPE1, 할인금액",
            "TYPE2, 할인율",
            "TYPE3, 배송비",
            "TYPE4, 무료쿠폰",
            "TYPE5, UP쿠폰"
    })
    @DisplayName("BrandTalkCouponType — type1~5 title 형식 검증")
    void shouldMapAllFiveTypes(String code, String expectedTitle) {
        // when
        BrandTalkCouponType couponType = BrandTalkCouponType.from(code);

        // then
        assertThat(couponType.name()).isEqualTo(code);
        assertThat(couponType.getTitle()).isEqualTo(expectedTitle);
    }

    @Test
    @DisplayName("BrandTalkCouponType.from(String) — 알 수 없는 값 시 예외")
    void shouldThrowExceptionForUnknownCouponType() {
        assertThatThrownBy(() -> BrandTalkCouponType.from("UNKNOWN"))
                .isInstanceOf(InvalidBrandTalkCouponTypeException.class);
    }
}
