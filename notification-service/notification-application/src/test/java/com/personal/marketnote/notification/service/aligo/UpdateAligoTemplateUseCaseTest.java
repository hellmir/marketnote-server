package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.UpdateAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.UpdateAligoTemplateResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
