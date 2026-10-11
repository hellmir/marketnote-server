package com.personal.marketnote.notification.domain.aligo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AligoTemplateTest {

    @Test
    @DisplayName("CreateState로 생성 시 inspStatus는 REG, templateStatus는 R로 초기화된다")
    void shouldInitializeWithRegAndWaitingStatus() {
        // given
        AligoTemplateCreateState state = AligoTemplateCreateState.builder()
                .senderKey("test-sender-key")
                .templateCode("TPL001")
                .templateName("테스트 템플릿")
                .templateContent("안녕하세요 #{이름}님")
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .build();

        // when
        AligoTemplate template = AligoTemplate.from(state);

        // then
        assertThat(template.getInspStatus()).isEqualTo("REG");
        assertThat(template.getTemplateStatus()).isEqualTo(AligoTemplateStatus.R);
        assertThat(template.canModify()).isTrue();
        assertThat(template.canDelete()).isTrue();
    }

    @Test
    @DisplayName("AD 타입 템플릿은 정상 생성된다")
    void shouldCreateAdTypeTemplate() {
        // given
        AligoTemplateCreateState state = AligoTemplateCreateState.builder()
                .senderKey("test-sender-key")
                .templateCode("TPL_AD")
                .templateName("광고 템플릿")
                .templateContent("광고 내용")
                .templateType(AligoTemplateType.AD)
                .templateEmType(AligoTemplateEmType.NONE)
                .templateAdvert(true)
                .build();

        // when
        AligoTemplate template = AligoTemplate.from(state);

        // then
        assertThat(template.getTemplateType().isAdType()).isTrue();
        assertThat(template.isTemplateAdvert()).isTrue();
    }

    @Test
    @DisplayName("버튼이 5개를 초과하면 예외가 발생한다")
    void shouldThrowExceptionWhenButtonsExceedMax() {
        // given
        List<AligoTemplateButton> buttons = List.of(
                AligoTemplateButton.builder().ordering(1).name("1").linkType(AligoTemplateButtonLinkType.WL).build(),
                AligoTemplateButton.builder().ordering(2).name("2").linkType(AligoTemplateButtonLinkType.WL).build(),
                AligoTemplateButton.builder().ordering(3).name("3").linkType(AligoTemplateButtonLinkType.WL).build(),
                AligoTemplateButton.builder().ordering(4).name("4").linkType(AligoTemplateButtonLinkType.WL).build(),
                AligoTemplateButton.builder().ordering(5).name("5").linkType(AligoTemplateButtonLinkType.WL).build(),
                AligoTemplateButton.builder().ordering(6).name("6").linkType(AligoTemplateButtonLinkType.WL).build()
        );
        AligoTemplateCreateState state = AligoTemplateCreateState.builder()
                .senderKey("test-sender-key")
                .templateCode("TPL001")
                .templateName("테스트")
                .templateContent("내용")
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .buttons(buttons)
                .build();

        // when & then
        assertThatThrownBy(() -> AligoTemplate.from(state))
                .isInstanceOf(InvalidAligoTemplateException.class);
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 검증 없이 모든 필드가 그대로 매핑된다")
    void shouldRestoreFromSnapshotWithoutValidation() {
        // given
        AligoTemplateSnapshotState state = AligoTemplateSnapshotState.builder()
                .id(1L)
                .senderKey("sender")
                .templateCode("TPL001")
                .templateName("이름")
                .templateContent("내용")
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .inspStatus("APR")
                .templateStatus(AligoTemplateStatus.A)
                .buttons(List.of())
                .build();

        // when
        AligoTemplate template = AligoTemplate.from(state);

        // then
        assertThat(template.getId()).isEqualTo(1L);
        assertThat(template.getInspStatus()).isEqualTo("APR");
        assertThat(template.getTemplateStatus().isActive()).isTrue();
    }

    @Test
    @DisplayName("canModify는 inspStatus가 REG 또는 REJ일 때만 true를 반환한다")
    void canModifyShouldReturnTrueOnlyForRegOrRej() {
        // given
        AligoTemplate regTemplate = createTemplateWithInspStatus("REG");
        AligoTemplate rejTemplate = createTemplateWithInspStatus("REJ");
        AligoTemplate aprTemplate = createTemplateWithInspStatus("APR");
        AligoTemplate reqTemplate = createTemplateWithInspStatus("REQ");

        // then
        assertThat(regTemplate.canModify()).isTrue();
        assertThat(rejTemplate.canModify()).isTrue();
        assertThat(aprTemplate.canModify()).isFalse();
        assertThat(reqTemplate.canModify()).isFalse();
    }

    @Test
    @DisplayName("수정 불가 상태에서 updateContent 호출 시 예외가 발생한다")
    void shouldThrowExceptionWhenUpdatingNonModifiableTemplate() {
        // given
        AligoTemplate template = createTemplateWithInspStatus("APR");

        // when & then
        assertThatThrownBy(() -> template.updateContent("이름", "내용", null, null, List.of()))
                .isInstanceOf(InvalidAligoTemplateModificationException.class);
    }

    @Test
    @DisplayName("삭제 불가 상태에서 validateDeletion 호출 시 예외가 발생한다")
    void shouldThrowExceptionWhenDeletingNonDeletableTemplate() {
        // given
        AligoTemplate template = createTemplateWithInspStatus("APR");

        // when & then
        assertThatThrownBy(() -> template.validateDeletion())
                .isInstanceOf(InvalidAligoTemplateDeletionException.class);
    }

    private AligoTemplate createTemplateWithInspStatus(String inspStatus) {
        return AligoTemplate.from(
                AligoTemplateSnapshotState.builder()
                        .id(1L)
                        .senderKey("sender")
                        .templateCode("TPL001")
                        .templateName("이름")
                        .templateContent("내용")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .inspStatus(inspStatus)
                        .templateStatus(AligoTemplateStatus.R)
                        .buttons(List.of())
                        .build()
        );
    }
}
