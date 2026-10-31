package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.AligoTemplateButtonCommand;
import com.personal.marketnote.notification.port.in.command.UpdateAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.UpdateAligoTemplateResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAligoTemplateUseCaseTest {

    @InjectMocks
    private UpdateAligoTemplateService updateAligoTemplateService;

    @Mock
    private FindAligoTemplatePort findAligoTemplatePort;

    @Mock
    private UpdateAligoTemplateExternalPort updateAligoTemplateExternalPort;

    @Mock
    private UpdateAligoTemplatePort updateAligoTemplatePort;

    @Test
    @DisplayName("수정 가능한 템플릿은 외부 API 호출 후 로컬 DB를 업데이트한다")
    void shouldUpdateModifiableTemplate() {
        // given
        AligoTemplate existing = createTemplate("REG");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(existing));

        UpdateAligoTemplateCommand command = new UpdateAligoTemplateCommand(
                "TPL001", "sender-key", "수정된 이름", "수정된 내용", null, null, List.of()
        );

        // when
        UpdateAligoTemplateResult result = updateAligoTemplateService.updateTemplate(command);

        // then
        assertThat(result.templateCode()).isEqualTo("TPL001");
        verify(updateAligoTemplateExternalPort).updateTemplate(any(AligoTemplate.class));
        verify(updateAligoTemplatePort).update(any(AligoTemplate.class));
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 수정 시 예외가 발생한다")
    void shouldThrowExceptionWhenTemplateNotFound() {
        // given
        when(findAligoTemplatePort.findByTemplateCode("UNKNOWN")).thenReturn(Optional.empty());

        UpdateAligoTemplateCommand command = new UpdateAligoTemplateCommand(
                "UNKNOWN", "sender-key", "이름", "내용", null, null, List.of()
        );

        // when & then
        assertThatThrownBy(() -> updateAligoTemplateService.updateTemplate(command))
                .isInstanceOf(AligoTemplateNotFoundException.class);
        verifyNoInteractions(updateAligoTemplateExternalPort, updateAligoTemplatePort);
    }

    @Test
    @DisplayName("수정 불가능한 템플릿(APR)은 InvalidAligoTemplateModificationException이 발생한다")
    void shouldThrowExceptionWhenTemplateNotModifiable() {
        // given
        AligoTemplate approved = createTemplate("APR");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(approved));

        UpdateAligoTemplateCommand command = new UpdateAligoTemplateCommand(
                "TPL001", "sender-key", "이름", "내용", null, null, List.of()
        );

        // when & then
        assertThatThrownBy(() -> updateAligoTemplateService.updateTemplate(command))
                .isInstanceOf(InvalidAligoTemplateModificationException.class);
        verifyNoInteractions(updateAligoTemplateExternalPort, updateAligoTemplatePort);
    }

    @Test
    @DisplayName("buttons가 NULL이면 빈 리스트로 처리하여 템플릿을 업데이트한다")
    void shouldHandleNullButtonsAsEmptyList() {
        // given
        AligoTemplate existing = createTemplate("REG");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(existing));

        UpdateAligoTemplateCommand command = new UpdateAligoTemplateCommand(
                "TPL001", "sender-key", "수정된 이름", "수정된 내용", null, null, null
        );

        // when
        UpdateAligoTemplateResult result = updateAligoTemplateService.updateTemplate(command);

        // then
        assertThat(result.templateCode()).isEqualTo("TPL001");
        assertThat(existing.getButtons()).isEmpty();
        verify(updateAligoTemplateExternalPort).updateTemplate(any(AligoTemplate.class));
        verify(updateAligoTemplatePort).update(any(AligoTemplate.class));
    }

    @Test
    @DisplayName("buttons가 빈 리스트이면 빈 리스트 그대로 템플릿을 업데이트한다")
    void shouldHandleEmptyButtonsList() {
        // given
        AligoTemplate existing = createTemplate("REG");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(existing));

        UpdateAligoTemplateCommand command = new UpdateAligoTemplateCommand(
                "TPL001", "sender-key", "수정된 이름", "수정된 내용", null, null, List.of()
        );

        // when
        updateAligoTemplateService.updateTemplate(command);

        // then
        assertThat(existing.getButtons()).isEmpty();
        verify(updateAligoTemplateExternalPort).updateTemplate(any(AligoTemplate.class));
        verify(updateAligoTemplatePort).update(any(AligoTemplate.class));
    }

    @Test
    @DisplayName("buttons가 존재하면 AligoTemplateButton으로 매핑되어 템플릿에 반영된다")
    void shouldMapButtonCommandsToTemplateButtons() {
        // given
        AligoTemplate existing = createTemplate("REG");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(existing));

        AligoTemplateButtonCommand buttonCommand = new AligoTemplateButtonCommand(
                1, "자세히 보기", "WL",
                "https://m.example.com", "https://www.example.com",
                null, null
        );
        UpdateAligoTemplateCommand command = new UpdateAligoTemplateCommand(
                "TPL001", "sender-key", "수정된 이름", "수정된 내용",
                null, null, List.of(buttonCommand)
        );

        // when
        updateAligoTemplateService.updateTemplate(command);

        // then
        assertThat(existing.getButtons()).hasSize(1);
        AligoTemplateButton mapped = existing.getButtons().get(0);
        assertThat(mapped.getOrdering()).isEqualTo(1);
        assertThat(mapped.getName()).isEqualTo("자세히 보기");
        assertThat(mapped.getLinkType()).isEqualTo(AligoTemplateButtonLinkType.WL);
        assertThat(mapped.getLinkMo()).isEqualTo("https://m.example.com");
    }

    @Test
    @DisplayName("외부 API 업데이트 후에 영속성 업데이트를 호출한다")
    void shouldCallExternalBeforePersistenceUpdate() {
        // given
        AligoTemplate existing = createTemplate("REG");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(existing));

        UpdateAligoTemplateCommand command = new UpdateAligoTemplateCommand(
                "TPL001", "sender-key", "수정된 이름", "수정된 내용", null, null, List.of()
        );

        // when
        updateAligoTemplateService.updateTemplate(command);

        // then
        InOrder inOrder = inOrder(updateAligoTemplateExternalPort, updateAligoTemplatePort);
        inOrder.verify(updateAligoTemplateExternalPort).updateTemplate(any(AligoTemplate.class));
        inOrder.verify(updateAligoTemplatePort).update(any(AligoTemplate.class));
    }

    @Test
    @DisplayName("외부 API 호출 실패 시 영속성 업데이트는 호출되지 않는다")
    void shouldNotCallPersistenceWhenExternalFails() {
        // given
        AligoTemplate existing = createTemplate("REG");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(existing));
        doThrow(new RuntimeException("외부 API 실패"))
                .when(updateAligoTemplateExternalPort).updateTemplate(any(AligoTemplate.class));

        UpdateAligoTemplateCommand command = new UpdateAligoTemplateCommand(
                "TPL001", "sender-key", "이름", "내용", null, null, List.of()
        );

        // when & then
        assertThatThrownBy(() -> updateAligoTemplateService.updateTemplate(command))
                .isInstanceOf(RuntimeException.class);
        verify(updateAligoTemplateExternalPort).updateTemplate(any(AligoTemplate.class));
        verifyNoInteractions(updateAligoTemplatePort);
    }

    private AligoTemplate createTemplate(String inspStatus) {
        return AligoTemplate.from(
                AligoTemplateSnapshotState.builder()
                        .id(1L)
                        .senderKey("sender-key")
                        .templateCode("TPL001")
                        .templateName("원본 이름")
                        .templateContent("원본 내용")
                        .templateType(AligoTemplateType.BA)
                        .templateEmType(AligoTemplateEmType.NONE)
                        .inspStatus(inspStatus)
                        .templateStatus(AligoTemplateStatus.R)
                        .buttons(List.of())
                        .build()
        );
    }
}
