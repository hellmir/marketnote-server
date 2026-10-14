package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkCouponTest {

    @Test
    @DisplayName("BrandTalkCoupon — description 최대 글자수 검증")
    void shouldThrowExceptionWhenDescriptionExceedsMaxLength() {
        // given
        String longDescription = "가".repeat(129);

        // when & then
        assertThatThrownBy(() -> BrandTalkCoupon.of(
                "할인 쿠폰", longDescription,
                "https://m.example.com", "https://www.example.com", null, null
        )).isInstanceOf(InvalidBrandTalkCouponException.class);
    }

    @Test
    @DisplayName("BrandTalkCoupon — 128자 이내 description은 정상 생성")
    void shouldCreateCouponWithValidDescription() {
        // given
        String validDescription = "가".repeat(128);

        // when
        BrandTalkCoupon coupon = BrandTalkCoupon.of(
                "할인 쿠폰", validDescription,
                "https://m.example.com", "https://www.example.com", null, null
        );

        // then
        assertThat(coupon.getTitle()).isEqualTo("할인 쿠폰");
        assertThat(coupon.getDescription()).isEqualTo(validDescription);
    }

    @Test
    @DisplayName("BrandTalkCoupon — title 필수 검증")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        assertThatThrownBy(() -> BrandTalkCoupon.of(
                null, "설명",
                null, null, null, null
        )).isInstanceOf(InvalidBrandTalkCouponException.class);
    }
}
