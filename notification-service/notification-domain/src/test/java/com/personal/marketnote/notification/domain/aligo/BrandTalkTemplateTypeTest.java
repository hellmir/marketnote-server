package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkTemplateTypeTest {

    @ParameterizedTest
    @CsvSource({
            "TEXT, 텍스트",
            "IMAGE, 이미지",
            "WIDE, 와이드",
            "WIDE_ITEM_LIST, 와이드 아이템 리스트",
            "CAROUSEL_FEED, 캐러셀 피드",
            "PREMIUM_VIDEO, 프리미엄 동영상",
            "COMMERCE, 커머스",
            "CAROUSEL_COMMERCE, 캐러셀 커머스"
    })
    @DisplayName("BrandTalkTemplateType.from(String) — 8종 정상 매핑")
    void shouldMapAllEightTypes(String code, String expectedDescription) {
        // when
        BrandTalkTemplateType type = BrandTalkTemplateType.from(code);

        // then
        assertThat(type.name()).isEqualTo(code);
        assertThat(type.getDescription()).isEqualTo(expectedDescription);
    }

    @Test
    @DisplayName("BrandTalkTemplateType.from(String) — 알 수 없는 값 시 예외")
    void shouldThrowExceptionForUnknownType() {
        assertThatThrownBy(() -> BrandTalkTemplateType.from("UNKNOWN"))
                .isInstanceOf(InvalidBrandTalkTemplateTypeException.class);
    }

    @Test
    @DisplayName("isCarousel은 CAROUSEL_FEED와 CAROUSEL_COMMERCE에서만 true를 반환한다")
    void isCarouselShouldReturnTrueOnlyForCarouselTypes() {
        assertThat(BrandTalkTemplateType.CAROUSEL_FEED.isCarousel()).isTrue();
        assertThat(BrandTalkTemplateType.CAROUSEL_COMMERCE.isCarousel()).isTrue();
        assertThat(BrandTalkTemplateType.TEXT.isCarousel()).isFalse();
        assertThat(BrandTalkTemplateType.IMAGE.isCarousel()).isFalse();
        assertThat(BrandTalkTemplateType.WIDE.isCarousel()).isFalse();
        assertThat(BrandTalkTemplateType.PREMIUM_VIDEO.isCarousel()).isFalse();
    }

    @Test
    @DisplayName("isPremiumVideo는 PREMIUM_VIDEO에서만 true를 반환한다")
    void isPremiumVideoShouldReturnTrueOnlyForPremiumVideo() {
        assertThat(BrandTalkTemplateType.PREMIUM_VIDEO.isPremiumVideo()).isTrue();
        assertThat(BrandTalkTemplateType.TEXT.isPremiumVideo()).isFalse();
        assertThat(BrandTalkTemplateType.CAROUSEL_FEED.isPremiumVideo()).isFalse();
    }

    @Test
    @DisplayName("isWideItemList는 WIDE_ITEM_LIST에서만 true를 반환한다")
    void isWideItemListShouldReturnTrueOnlyForWideItemList() {
        assertThat(BrandTalkTemplateType.WIDE_ITEM_LIST.isWideItemList()).isTrue();
        assertThat(BrandTalkTemplateType.WIDE.isWideItemList()).isFalse();
        assertThat(BrandTalkTemplateType.TEXT.isWideItemList()).isFalse();
    }
}
