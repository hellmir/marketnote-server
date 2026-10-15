package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkCarouselItemTest {

    @Test
    @DisplayName("캐러셀 아이템의 버튼이 최소 1개 미만이면 예외가 발생한다")
    void shouldThrowExceptionWhenButtonsEmpty() {
        // when & then
        assertThatThrownBy(() -> BrandTalkCarouselItem.of("제목", "내용", null, List.of(), null))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("최소 1개");
    }

    @Test
    @DisplayName("캐러셀 아이템의 버튼이 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenButtonsNull() {
        // when & then
        assertThatThrownBy(() -> BrandTalkCarouselItem.of("제목", "내용", null, null, null))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("최소 1개");
    }

    @Test
    @DisplayName("캐러셀 아이템의 버튼이 3개(최대 2개 초과)이면 예외가 발생한다")
    void shouldThrowExceptionWhenButtonsExceedMax() {
        // given
        BrandTalkButton button1 = BrandTalkButton.of("버튼1", AligoTemplateButtonLinkType.WL, "https://m.ex.com", "https://www.ex.com", null, null);
        BrandTalkButton button2 = BrandTalkButton.of("버튼2", AligoTemplateButtonLinkType.WL, "https://m.ex.com", "https://www.ex.com", null, null);
        BrandTalkButton button3 = BrandTalkButton.of("버튼3", AligoTemplateButtonLinkType.WL, "https://m.ex.com", "https://www.ex.com", null, null);

        // when & then
        assertThatThrownBy(() -> BrandTalkCarouselItem.of("제목", "내용", null, List.of(button1, button2, button3), null))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("최대 2개");
    }

    @Test
    @DisplayName("캐러셀 아이템 버튼 1개로 정상 생성된다")
    void shouldCreateWithOneButton() {
        // given
        BrandTalkButton button = BrandTalkButton.of("버튼", AligoTemplateButtonLinkType.WL, "https://m.ex.com", "https://www.ex.com", null, null);

        // when
        BrandTalkCarouselItem item = BrandTalkCarouselItem.of("제목", "내용", "https://img.com/1.jpg", List.of(button), null);

        // then
        assertThat(item.getHeader()).isEqualTo("제목");
        assertThat(item.getContent()).isEqualTo("내용");
        assertThat(item.getImageUrl()).isEqualTo("https://img.com/1.jpg");
        assertThat(item.getButtons()).hasSize(1);
    }

    @Test
    @DisplayName("캐러셀 아이템 버튼 2개 + 쿠폰으로 정상 생성된다")
    void shouldCreateWithTwoButtonsAndCoupon() {
        // given
        BrandTalkButton button1 = BrandTalkButton.of("버튼1", AligoTemplateButtonLinkType.WL, "https://m.ex.com", "https://www.ex.com", null, null);
        BrandTalkButton button2 = BrandTalkButton.of("버튼2", AligoTemplateButtonLinkType.BK, null, null, null, null);
        BrandTalkCoupon coupon = BrandTalkCoupon.of("10% 할인", null, "https://m.ex.com", "https://www.ex.com", null, null);

        // when
        BrandTalkCarouselItem item = BrandTalkCarouselItem.of("제목", "내용", null, List.of(button1, button2), coupon);

        // then
        assertThat(item.getButtons()).hasSize(2);
        assertThat(item.getCoupon()).isNotNull();
        assertThat(item.getCoupon().getTitle()).isEqualTo("10% 할인");
    }
}
