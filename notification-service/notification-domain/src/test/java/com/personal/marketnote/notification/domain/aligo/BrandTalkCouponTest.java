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

    @Test
    @DisplayName("BrandTalkCoupon — SnapshotState로 생성 시 검증 없이 필드를 매핑한다")
    void shouldRestoreFromSnapshotWithoutValidation() {
        // given: invariant 위반(title null, description 최대 길이 초과)도 복원 성공해야 함
        String longDescription = "가".repeat(500);

        // when
        BrandTalkCoupon coupon = BrandTalkCoupon.from(
                BrandTalkCouponSnapshotState.builder()
                        .title(null)
                        .description(longDescription)
                        .linkM("https://m.example.com")
                        .linkP("https://www.example.com")
                        .linkI("example://open")
                        .linkA("android://open")
                        .build()
        );

        // then
        assertThat(coupon.getTitle()).isNull();
        assertThat(coupon.getDescription()).isEqualTo(longDescription);
        assertThat(coupon.getLinkM()).isEqualTo("https://m.example.com");
        assertThat(coupon.getLinkP()).isEqualTo("https://www.example.com");
        assertThat(coupon.getLinkI()).isEqualTo("example://open");
        assertThat(coupon.getLinkA()).isEqualTo("android://open");
    }
}
