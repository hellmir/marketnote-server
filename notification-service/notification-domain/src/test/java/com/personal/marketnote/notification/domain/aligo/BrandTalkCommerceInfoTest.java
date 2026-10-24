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
}
