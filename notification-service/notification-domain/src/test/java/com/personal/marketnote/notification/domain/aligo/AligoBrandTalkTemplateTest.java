package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoBrandTalkTemplateTest {

    @Test
    @DisplayName("TEXT 타입 브랜드메시지 템플릿을 정상 생성한다")
    void shouldCreateTextTemplate() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("test-sender-key")
                .templateCode("BT_TEXT_001")
                .templateName("텍스트 템플릿")
                .templateType(BrandTalkTemplateType.TEXT)
                .templateContent("안녕하세요 #{이름}님")
                .build();

        // when
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(state);

        // then
        assertThat(template.getTemplateCode()).isEqualTo("BT_TEXT_001");
        assertThat(template.getTemplateType()).isEqualTo(BrandTalkTemplateType.TEXT);
        assertThat(template.isActive()).isTrue();
        assertThat(template.requiresImage()).isFalse();
    }

    @Test
    @DisplayName("IMAGE 타입 브랜드메시지 템플릿을 정상 생성하고 requiresImage가 true를 반환한다")
    void shouldCreateImageTemplateAndRequireImage() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("test-sender-key")
                .templateCode("BT_IMAGE_001")
                .templateName("이미지 템플릿")
                .templateType(BrandTalkTemplateType.IMAGE)
                .templateContent("이미지 내용")
                .build();

        // when
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(state);

        // then
        assertThat(template.getTemplateType()).isEqualTo(BrandTalkTemplateType.IMAGE);
        assertThat(template.requiresImage()).isTrue();
    }

    @Test
    @DisplayName("WIDE 타입 브랜드메시지 템플릿을 정상 생성하고 requiresImage가 true를 반환한다")
    void shouldCreateWideTemplateAndRequireImage() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("test-sender-key")
                .templateCode("BT_WIDE_001")
                .templateName("와이드 템플릿")
                .templateType(BrandTalkTemplateType.WIDE)
                .templateContent("와이드 내용")
                .build();

        // when
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(state);

        // then
        assertThat(template.getTemplateType()).isEqualTo(BrandTalkTemplateType.WIDE);
        assertThat(template.requiresImage()).isTrue();
    }

    @Test
    @DisplayName("senderKey가 누락되면 예외가 발생한다")
    void shouldThrowExceptionWhenSenderKeyMissing() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .templateCode("BT_TEXT_001")
                .templateName("이름")
                .templateType(BrandTalkTemplateType.TEXT)
                .templateContent("내용")
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class);
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 검증 없이 모든 필드가 그대로 매핑된다")
    void shouldRestoreFromSnapshotWithoutValidation() {
        // given
        AligoBrandTalkTemplateSnapshotState state = AligoBrandTalkTemplateSnapshotState.builder()
                .id(1L)
                .senderKey("sender")
                .templateCode("BT_TEXT_001")
                .templateName("이름")
                .templateType(BrandTalkTemplateType.TEXT)
                .templateContent("내용")
                .adult(false)
                .buttons(List.of())
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        // when
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(state);

        // then
        assertThat(template.getId()).isEqualTo(1L);
        assertThat(template.getTemplateCode()).isEqualTo("BT_TEXT_001");
        assertThat(template.isActive()).isTrue();
    }

    private BrandTalkButton createTestButton() {
        return BrandTalkButton.of("버튼", AligoTemplateButtonLinkType.WL, "https://m.ex.com", "https://www.ex.com", null, null);
    }

    private List<BrandTalkCarouselItem> createCarouselItems(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> BrandTalkCarouselItem.of("제목" + i, "내용" + i, null, List.of(createTestButton()), null))
                .toList();
    }

    @Test
    @DisplayName("CAROUSEL_FEED carousel_header 최대 20자 초과 시 예외가 발생한다")
    void shouldThrowExceptionWhenCarouselHeaderExceedsMaxLength() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender")
                .templateCode("BT_CF_001")
                .templateName("캐러셀 피드")
                .templateType(BrandTalkTemplateType.CAROUSEL_FEED)
                .templateContent("내용")
                .carouselHeader("가나다라마바사아자차카타파하가나다라마바사")
                .carouselItems(createCarouselItems(3))
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("carouselHeader");
    }

    @Test
    @DisplayName("CAROUSEL_FEED 인트로(header) 사용 시 carouselItems 최대 5개 검증한다")
    void shouldValidateMaxCarouselItemsWithIntro() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender")
                .templateCode("BT_CF_002")
                .templateName("캐러셀 피드")
                .templateType(BrandTalkTemplateType.CAROUSEL_FEED)
                .templateContent("내용")
                .carouselHeader("인트로 헤더")
                .carouselItems(createCarouselItems(6))
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("인트로 사용 시");
    }

    @Test
    @DisplayName("CAROUSEL_FEED 인트로 미사용 시 carouselItems 최대 6개 허용한다")
    void shouldAllowMaxSixCarouselItemsWithoutIntro() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender")
                .templateCode("BT_CF_003")
                .templateName("캐러셀 피드")
                .templateType(BrandTalkTemplateType.CAROUSEL_FEED)
                .templateContent("내용")
                .carouselItems(createCarouselItems(6))
                .build();

        // when
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(state);

        // then
        assertThat(template.getCarouselItems()).hasSize(6);
        assertThat(template.isCarousel()).isTrue();
    }

    @Test
    @DisplayName("CAROUSEL_FEED carouselItems 0개 시 예외가 발생한다")
    void shouldThrowExceptionWhenCarouselItemsEmpty() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender")
                .templateCode("BT_CF_004")
                .templateName("캐러셀 피드")
                .templateType(BrandTalkTemplateType.CAROUSEL_FEED)
                .templateContent("내용")
                .carouselItems(List.of())
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("carouselItems가 필수");
    }

    @Test
    @DisplayName("CAROUSEL_COMMERCE carousel_list_commerce 누락 시 예외가 발생한다")
    void shouldThrowExceptionWhenCarouselCommercesMissing() {
        // given
        AligoBrandTalkTemplateCreateState state = AligoBrandTalkTemplateCreateState.builder()
                .senderKey("sender")
                .templateCode("BT_CC_001")
                .templateName("캐러셀 커머스")
                .templateType(BrandTalkTemplateType.CAROUSEL_COMMERCE)
                .templateContent("내용")
                .carouselItems(createCarouselItems(3))
                .build();

        // when & then
        assertThatThrownBy(() -> AligoBrandTalkTemplate.from(state))
                .isInstanceOf(InvalidBrandTalkTemplateException.class)
                .hasMessageContaining("carouselCommerces가 필수");
    }
}
