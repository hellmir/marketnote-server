package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkCarouselCommerceTest {

    @Test
    @DisplayName("캐러셀 커머스 title 누락 시 예외가 발생한다")
    void shouldThrowExceptionWhenTitleMissing() {
        // when & then
        assertThatThrownBy(() -> BrandTalkCarouselCommerce.of(null, "10000", null, null, null))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("title은 필수");
    }

    @Test
    @DisplayName("캐러셀 커머스 regularPrice 누락 시 예외가 발생한다")
    void shouldThrowExceptionWhenRegularPriceMissing() {
        // when & then
        assertThatThrownBy(() -> BrandTalkCarouselCommerce.of("상품명", null, null, null, null))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("regularPrice는 필수");
    }

    @Test
    @DisplayName("캐러셀 커머스 title + regularPrice만으로 정상 생성된다")
    void shouldCreateWithRequiredFieldsOnly() {
        // when
        BrandTalkCarouselCommerce commerce = BrandTalkCarouselCommerce.of("상품명", "10000", null, null, null);

        // then
        assertThat(commerce.getTitle()).isEqualTo("상품명");
        assertThat(commerce.getRegularPrice()).isEqualTo("10000");
        assertThat(commerce.getDiscountPrice()).isNull();
    }

    @Test
    @DisplayName("캐러셀 커머스 할인 정보 포함하여 정상 생성된다")
    void shouldCreateWithDiscountInfo() {
        // when
        BrandTalkCarouselCommerce commerce = BrandTalkCarouselCommerce.of("상품명", "10000", "8000", "20", null);

        // then
        assertThat(commerce.getDiscountPrice()).isEqualTo("8000");
        assertThat(commerce.getDiscountRate()).isEqualTo("20");
    }
}
