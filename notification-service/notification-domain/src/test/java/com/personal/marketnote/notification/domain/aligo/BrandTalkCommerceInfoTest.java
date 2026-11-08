package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkCommerceInfoTest {

    @Test
    @DisplayName("커머스 title 누락 시 예외가 발생한다")
    void shouldThrowExceptionWhenTitleMissing() {
        assertThatThrownBy(() -> BrandTalkCommerceInfo.from(BrandTalkCommerceInfoCreateState.builder().title(null).regularPrice("10000").discountPrice(null).discountRate(null).discountFixed(null).build()))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("title은 필수");
    }

    @Test
    @DisplayName("커머스 regularPrice 누락 시 예외가 발생한다")
    void shouldThrowExceptionWhenRegularPriceMissing() {
        assertThatThrownBy(() -> BrandTalkCommerceInfo.from(BrandTalkCommerceInfoCreateState.builder().title("상품명").regularPrice(null).discountPrice(null).discountRate(null).discountFixed(null).build()))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("regularPrice는 필수");
    }

    @Test
    @DisplayName("커머스 title + regularPrice만으로 정상 생성된다")
    void shouldCreateWithRequiredFieldsOnly() {
        BrandTalkCommerceInfo info = BrandTalkCommerceInfo.from(BrandTalkCommerceInfoCreateState.builder().title("상품명").regularPrice("10000").discountPrice(null).discountRate(null).discountFixed(null).build());
        assertThat(info.getTitle()).isEqualTo("상품명");
        assertThat(info.getRegularPrice()).isEqualTo("10000");
    }

    @Test
    @DisplayName("BrandTalkCommerceInfo — SnapshotState로 생성 시 검증 없이 필드를 매핑한다")
    void shouldRestoreFromSnapshotWithoutValidation() {
        // given: invariant 위반(title null, regularPrice null)도 복원 성공해야 함
        // when
        BrandTalkCommerceInfo info = BrandTalkCommerceInfo.from(
                BrandTalkCommerceInfoSnapshotState.builder()
                        .title(null)
                        .regularPrice(null)
                        .discountPrice("8000")
                        .discountRate("20")
                        .discountFixed("2000")
                        .build()
        );

        // then
        assertThat(info.getTitle()).isNull();
        assertThat(info.getRegularPrice()).isNull();
        assertThat(info.getDiscountPrice()).isEqualTo("8000");
        assertThat(info.getDiscountRate()).isEqualTo("20");
        assertThat(info.getDiscountFixed()).isEqualTo("2000");
    }
}
