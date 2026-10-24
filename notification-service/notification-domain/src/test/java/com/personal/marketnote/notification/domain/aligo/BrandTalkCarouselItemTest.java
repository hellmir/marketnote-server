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
        assertThatThrownBy(() -> BrandTalkCarouselItem.from(BrandTalkCarouselItemCreateState.builder().header("제목").content("내용").imageUrl(null).buttons(List.of()).coupon(null).build()))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("최소 1개");
    }

    @Test
    @DisplayName("캐러셀 아이템의 버튼이 null이면 예외가 발생한다")
    void shouldThrowExceptionWhenButtonsNull() {
        // when & then
        assertThatThrownBy(() -> BrandTalkCarouselItem.from(BrandTalkCarouselItemCreateState.builder().header("제목").content("내용").imageUrl(null).buttons(null).coupon(null).build()))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("최소 1개");
    }

    @Test
    @DisplayName("캐러셀 아이템의 버튼이 3개(최대 2개 초과)이면 예외가 발생한다")
    void shouldThrowExceptionWhenButtonsExceedMax() {
        // given
        BrandTalkButton button1 = BrandTalkButton.from(BrandTalkButtonCreateState.builder().name("버튼1").linkType(AligoTemplateButtonLinkType.WL).linkM("https://m.ex.com").linkP("https://www.ex.com").linkI(null).linkA(null).build());
        BrandTalkButton button2 = BrandTalkButton.from(BrandTalkButtonCreateState.builder().name("버튼2").linkType(AligoTemplateButtonLinkType.WL).linkM("https://m.ex.com").linkP("https://www.ex.com").linkI(null).linkA(null).build());
        BrandTalkButton button3 = BrandTalkButton.from(BrandTalkButtonCreateState.builder().name("버튼3").linkType(AligoTemplateButtonLinkType.WL).linkM("https://m.ex.com").linkP("https://www.ex.com").linkI(null).linkA(null).build());

        // when & then
        assertThatThrownBy(() -> BrandTalkCarouselItem.from(BrandTalkCarouselItemCreateState.builder().header("제목").content("내용").imageUrl(null).buttons(List.of(button1, button2, button3)).coupon(null).build()))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("최대 2개");
    }

    @Test
    @DisplayName("캐러셀 아이템 버튼 1개로 정상 생성된다")
    void shouldCreateWithOneButton() {
        // given
        BrandTalkButton button = BrandTalkButton.from(BrandTalkButtonCreateState.builder().name("버튼").linkType(AligoTemplateButtonLinkType.WL).linkM("https://m.ex.com").linkP("https://www.ex.com").linkI(null).linkA(null).build());

        // when
        BrandTalkCarouselItem item = BrandTalkCarouselItem.from(BrandTalkCarouselItemCreateState.builder().header("제목").content("내용").imageUrl("https://img.com/1.jpg").buttons(List.of(button)).coupon(null).build());

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
        BrandTalkButton button1 = BrandTalkButton.from(BrandTalkButtonCreateState.builder().name("버튼1").linkType(AligoTemplateButtonLinkType.WL).linkM("https://m.ex.com").linkP("https://www.ex.com").linkI(null).linkA(null).build());
        BrandTalkButton button2 = BrandTalkButton.from(BrandTalkButtonCreateState.builder().name("버튼2").linkType(AligoTemplateButtonLinkType.BK).linkM(null).linkP(null).linkI(null).linkA(null).build());
        BrandTalkCoupon coupon = BrandTalkCoupon.from(BrandTalkCouponCreateState.builder().title("10% 할인").description(null).linkM("https://m.ex.com").linkP("https://www.ex.com").linkI(null).linkA(null).build());

        // when
        BrandTalkCarouselItem item = BrandTalkCarouselItem.from(BrandTalkCarouselItemCreateState.builder().header("제목").content("내용").imageUrl(null).buttons(List.of(button1, button2)).coupon(coupon).build());

        // then
        assertThat(item.getButtons()).hasSize(2);
        assertThat(item.getCoupon()).isNotNull();
        assertThat(item.getCoupon().getTitle()).isEqualTo("10% 할인");
    }
}
