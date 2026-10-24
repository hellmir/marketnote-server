package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrandTalkWideItemTest {

    @Test
    @DisplayName("WIDE_ITEM_LIST 템플릿에서 templateHeader가 20자를 초과하면 예외가 발생한다")
    void shouldThrowExceptionWhenTemplateHeaderExceedsMaxLength() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender-key")
                .templateCode("BT_WIL_001")
                .templateName("와이드 아이템 리스트")
                .templateType(BrandTalkTemplateType.WIDE_ITEM_LIST)
                .templateContent("내용")
                .templateHeader("가".repeat(21))
                .subWideItems(List.of(
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브1").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build()),
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브2").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build())
                ))
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class);
    }

    @Test
    @DisplayName("WIDE_ITEM_LIST 템플릿에서 templateHeader에 줄바꿈이 포함되면 예외가 발생한다")
    void shouldThrowExceptionWhenTemplateHeaderContainsNewline() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender-key")
                .templateCode("BT_WIL_002")
                .templateName("와이드 아이템 리스트")
                .templateType(BrandTalkTemplateType.WIDE_ITEM_LIST)
                .templateContent("내용")
                .templateHeader("헤더\n줄바꿈")
                .subWideItems(List.of(
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브1").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build()),
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브2").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build())
                ))
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class);
    }

    @Test
    @DisplayName("WIDE_ITEM_LIST 템플릿에서 subWideItems가 1개면 예외가 발생한다")
    void shouldThrowExceptionWhenSubWideItemsLessThanTwo() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender-key")
                .templateCode("BT_WIL_003")
                .templateName("와이드 아이템 리스트")
                .templateType(BrandTalkTemplateType.WIDE_ITEM_LIST)
                .templateContent("내용")
                .subWideItems(List.of(
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브1").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build())
                ))
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class);
    }

    @Test
    @DisplayName("WIDE_ITEM_LIST 템플릿에서 subWideItems가 4개면 예외가 발생한다")
    void shouldThrowExceptionWhenSubWideItemsMoreThanThree() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender-key")
                .templateCode("BT_WIL_004")
                .templateName("와이드 아이템 리스트")
                .templateType(BrandTalkTemplateType.WIDE_ITEM_LIST)
                .templateContent("내용")
                .subWideItems(List.of(
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브1").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build()),
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브2").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build()),
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브3").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build()),
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브4").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build())
                ))
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class);
    }

    @Test
    @DisplayName("WIDE_ITEM_LIST 템플릿을 정상 생성한다")
    void shouldCreateWideItemListTemplate() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender-key")
                .templateCode("BT_WIL_005")
                .templateName("와이드 아이템 리스트")
                .templateType(BrandTalkTemplateType.WIDE_ITEM_LIST)
                .templateContent("내용")
                .templateHeader("20자이내헤더")
                .mainWideItem(BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("메인").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build()))
                .subWideItems(List.of(
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브1").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build()),
                        BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title("서브2").imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build())
                ))
                .build();

        // when
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(state);

        // then
        assertThat(template.isWideItemList()).isTrue();
        assertThat(template.getTemplateHeader()).isEqualTo("20자이내헤더");
        assertThat(template.getMainWideItem().getTitle()).isEqualTo("메인");
        assertThat(template.getSubWideItems()).hasSize(2);
    }

    @Test
    @DisplayName("BrandTalkWideItem의 title이 누락되면 예외가 발생한다")
    void shouldThrowExceptionWhenWideItemTitleMissing() {
        assertThatThrownBy(() -> BrandTalkWideItem.from(BrandTalkWideItemCreateState.builder().title(null).imageUrl(null).linkM(null).linkP(null).linkI(null).linkA(null).build()))
                .isInstanceOf(InvalidBrandTalkTemplateException.class);
    }
}
