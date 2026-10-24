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
        assertThatThrownBy(() -> BrandTalkCoupon.from(BrandTalkCouponCreateState.builder().title("할인 쿠폰").description(longDescription).linkM("https://m.example.com").linkP("https://www.example.com").linkI(null).linkA(null).build())).isInstanceOf(InvalidBrandTalkCouponException.class);
    }

    @Test
    @DisplayName("BrandTalkCoupon — 128자 이내 description은 정상 생성")
    void shouldCreateCouponWithValidDescription() {
        // given
        String validDescription = "가".repeat(128);

        // when
        BrandTalkCoupon coupon = BrandTalkCoupon.from(BrandTalkCouponCreateState.builder().title("할인 쿠폰").description(validDescription).linkM("https://m.example.com").linkP("https://www.example.com").linkI(null).linkA(null).build());

        // then
        assertThat(coupon.getTitle()).isEqualTo("할인 쿠폰");
        assertThat(coupon.getDescription()).isEqualTo(validDescription);
    }

    @Test
    @DisplayName("BrandTalkCoupon — title 필수 검증")
    void shouldThrowExceptionWhenTitleIsEmpty() {
        assertThatThrownBy(() -> BrandTalkCoupon.from(BrandTalkCouponCreateState.builder().title(null).description("설명").linkM(null).linkP(null).linkI(null).linkA(null).build())).isInstanceOf(InvalidBrandTalkCouponException.class);
    }
}
