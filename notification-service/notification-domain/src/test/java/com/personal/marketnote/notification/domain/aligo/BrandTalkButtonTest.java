package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkButtonTest {

    @Test
    @DisplayName("BrandTalkButton — WL 타입일 때 linkM/linkP 필수 검증")
    void shouldRequireLinkMAndLinkPForWlType() {
        assertThatThrownBy(() -> BrandTalkButton.of(
                "웹링크 버튼", AligoTemplateButtonLinkType.WL,
                null, null, null, null
        )).isInstanceOf(InvalidBrandTalkButtonException.class);
    }

    @Test
    @DisplayName("BrandTalkButton — WL 타입에 linkM/linkP 제공 시 정상 생성")
    void shouldCreateWlButtonWithLinks() {
        // when
        BrandTalkButton button = BrandTalkButton.of(
                "웹링크 버튼", AligoTemplateButtonLinkType.WL,
                "https://m.example.com", "https://www.example.com", null, null
        );

        // then
        assertThat(button.getName()).isEqualTo("웹링크 버튼");
        assertThat(button.getLinkType()).isEqualTo(AligoTemplateButtonLinkType.WL);
        assertThat(button.getLinkM()).isEqualTo("https://m.example.com");
        assertThat(button.getLinkP()).isEqualTo("https://www.example.com");
    }

    @Test
    @DisplayName("BrandTalkButton — AL 타입일 때 linkI/linkA 필수 검증")
    void shouldRequireLinkIAndLinkAForAlType() {
        assertThatThrownBy(() -> BrandTalkButton.of(
                "앱링크 버튼", AligoTemplateButtonLinkType.AL,
                null, null, null, null
        )).isInstanceOf(InvalidBrandTalkButtonException.class);
    }

    @Test
    @DisplayName("BrandTalkButton — AL 타입에 linkI/linkA 제공 시 정상 생성")
    void shouldCreateAlButtonWithLinks() {
        // when
        BrandTalkButton button = BrandTalkButton.of(
                "앱링크 버튼", AligoTemplateButtonLinkType.AL,
                null, null, "ios://app", "android://app"
        );

        // then
        assertThat(button.getName()).isEqualTo("앱링크 버튼");
        assertThat(button.getLinkType()).isEqualTo(AligoTemplateButtonLinkType.AL);
        assertThat(button.getLinkI()).isEqualTo("ios://app");
        assertThat(button.getLinkA()).isEqualTo("android://app");
    }
}
