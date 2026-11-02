package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.DeleteAligoTemplateCommand;
import com.personal.marketnote.notification.port.out.aligo.DeleteAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.DeleteAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteAligoTemplateUseCaseTest {

    @InjectMocks
    private DeleteAligoTemplateService deleteAligoTemplateService;

    @Mock
    private FindAligoTemplatePort findAligoTemplatePort;

    @Mock
    private DeleteAligoTemplateExternalPort deleteAligoTemplateExternalPort;

    @Mock
    private DeleteAligoTemplatePort deleteAligoTemplatePort;

    @Test
    @DisplayName("삭제 가능한 템플릿은 외부 API 호출 후 로컬 DB에서 삭제한다")
    void shouldDeleteDeletableTemplate() {
        // given
        AligoTemplate template = createTemplate("REG");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(template));

        DeleteAligoTemplateCommand command = new DeleteAligoTemplateCommand("TPL001", "sender-key");

        // when
        deleteAligoTemplateService.deleteTemplate(command);

        // then
        verify(deleteAligoTemplateExternalPort).deleteTemplate("sender-key", "TPL001");
        verify(deleteAligoTemplatePort).deleteByTemplateCode("TPL001");
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 삭제 시 예외가 발생한다")
    void shouldThrowExceptionWhenTemplateNotFound() {
        // given
        when(findAligoTemplatePort.findByTemplateCode("UNKNOWN")).thenReturn(Optional.empty());

        DeleteAligoTemplateCommand command = new DeleteAligoTemplateCommand("UNKNOWN", "sender-key");

        // when & then
        assertThatThrownBy(() -> deleteAligoTemplateService.deleteTemplate(command))
                .isInstanceOf(AligoTemplateNotFoundException.class);
        verifyNoInteractions(deleteAligoTemplateExternalPort, deleteAligoTemplatePort);
    }

    @Test
    @DisplayName("삭제 불가능한 템플릿(APR)은 InvalidAligoTemplateDeletionException이 발생한다")
    void shouldThrowExceptionWhenTemplateNotDeletable() {
        // given
        AligoTemplate template = createTemplate("APR");
        when(findAligoTemplatePort.findByTemplateCode("TPL001")).thenReturn(Optional.of(template));

        DeleteAligoTemplateCommand command = new DeleteAligoTemplateCommand("TPL001", "sender-key");

        // when & then
        assertThatThrownBy(() -> deleteAligoTemplateService.deleteTemplate(command))
                .isInstanceOf(InvalidAligoTemplateDeletionException.class);
        verifyNoInteractions(deleteAligoTemplateExternalPort, deleteAligoTemplatePort);
    }

    private AligoTemplate createTemplate(String inspStatus) {
        return AligoTemplate.from(
                AligoTemplateSnapshotState.builder()
                        .id(1L)
                        .senderKey("sender-key")
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
