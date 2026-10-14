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
}
