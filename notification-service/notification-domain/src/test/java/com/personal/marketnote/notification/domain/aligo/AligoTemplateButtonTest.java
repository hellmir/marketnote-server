package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AligoTemplateButtonTest {

    @Test
    @DisplayName("CreateState로부터 AligoTemplateButton을 생성한다")
    void shouldCreateFromState() {
        // given
        AligoTemplateButtonCreateState state = AligoTemplateButtonCreateState.builder()
                .ordering(1)
                .name("웹링크 버튼")
                .linkType(AligoTemplateButtonLinkType.WL)
                .linkMo("https://m.example.com")
                .linkPc("https://www.example.com")
                .linkIos(null)
                .linkAnd(null)
                .build();

        // when
        AligoTemplateButton button = AligoTemplateButton.from(state);

        // then
        assertThat(button.getOrdering()).isEqualTo(1);
        assertThat(button.getName()).isEqualTo("웹링크 버튼");
        assertThat(button.getLinkType()).isEqualTo(AligoTemplateButtonLinkType.WL);
        assertThat(button.getLinkMo()).isEqualTo("https://m.example.com");
        assertThat(button.getLinkPc()).isEqualTo("https://www.example.com");
        assertThat(button.getLinkIos()).isNull();
        assertThat(button.getLinkAnd()).isNull();
    }

    @Test
    @DisplayName("AL 타입 버튼은 linkIos/linkAnd를 포함하여 생성된다")
    void shouldCreateAlButtonWithAppLinks() {
        // given
        AligoTemplateButtonCreateState state = AligoTemplateButtonCreateState.builder()
                .ordering(2)
                .name("앱링크 버튼")
                .linkType(AligoTemplateButtonLinkType.AL)
                .linkMo(null)
                .linkPc(null)
                .linkIos("ios://app")
                .linkAnd("android://app")
                .build();

        // when
        AligoTemplateButton button = AligoTemplateButton.from(state);

        // then
        assertThat(button.getOrdering()).isEqualTo(2);
        assertThat(button.getName()).isEqualTo("앱링크 버튼");
        assertThat(button.getLinkType()).isEqualTo(AligoTemplateButtonLinkType.AL);
        assertThat(button.getLinkIos()).isEqualTo("ios://app");
        assertThat(button.getLinkAnd()).isEqualTo("android://app");
    }
}
