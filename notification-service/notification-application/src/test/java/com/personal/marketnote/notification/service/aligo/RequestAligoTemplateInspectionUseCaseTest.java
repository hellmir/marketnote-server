package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.result.aligo.RequestAligoTemplateInspectionResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.RequestAligoTemplateInspectionExternalPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestAligoTemplateInspectionUseCaseTest {

    @InjectMocks
    private RequestAligoTemplateInspectionService requestAligoTemplateInspectionService;

    @Mock
    private FindAligoTemplatePort findAligoTemplatePort;

    @Mock
    private RequestAligoTemplateInspectionExternalPort requestAligoTemplateInspectionExternalPort;

    @Mock
    private UpdateAligoTemplatePort updateAligoTemplatePort;

    @Test
    @DisplayName("검수요청 가능 상태(REG)에서 정상 호출 시 외부 API 호출 후 markAsRequested를 수행한다")
    void shouldRequestInspectionWhenTemplateIsRegistered() {
        // given
        String templateCode = "TPL001";
        AligoTemplate template = createTemplate(templateCode, "REG");
        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));

        // when
        RequestAligoTemplateInspectionResult result = requestAligoTemplateInspectionService.requestInspection(templateCode);

        // then
        assertThat(result.templateCode()).isEqualTo(templateCode);
        assertThat(result.inspStatus()).isEqualTo("REQ");
        verify(requestAligoTemplateInspectionExternalPort).requestInspection("sender-key-1", templateCode);
        verify(updateAligoTemplatePort).update(template);
    }

    @Test
    @DisplayName("검수요청 불가 상태(APR)에서 호출 시 InvalidAligoTemplateTransitionException을 던진다")
    void shouldThrowExceptionWhenCannotRequestInspection() {
        // given
        String templateCode = "TPL001";
        AligoTemplate template = createTemplate(templateCode, "APR");
        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.of(template));

        // when & then
        assertThatThrownBy(() -> requestAligoTemplateInspectionService.requestInspection(templateCode))
                .isInstanceOf(InvalidAligoTemplateTransitionException.class);
        verifyNoInteractions(requestAligoTemplateInspectionExternalPort);
        verifyNoInteractions(updateAligoTemplatePort);
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 코드로 검수요청 시 AligoTemplateNotFoundException을 던진다")
    void shouldThrowExceptionWhenTemplateNotFound() {
        // given
        String templateCode = "NONEXISTENT";
        when(findAligoTemplatePort.findByTemplateCode(templateCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> requestAligoTemplateInspectionService.requestInspection(templateCode))
                .isInstanceOf(AligoTemplateNotFoundException.class);
        verifyNoInteractions(requestAligoTemplateInspectionExternalPort);
        verifyNoInteractions(updateAligoTemplatePort);
    }

    private AligoTemplate createTemplate(String templateCode, String inspStatus) {
        return AligoTemplate.from(AligoTemplateSnapshotState.builder()
                .id(1L)
                .senderKey("sender-key-1")
                .templateCode(templateCode)
                .templateName("테스트 템플릿")
                .templateContent("안녕하세요")
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .inspStatus(inspStatus)
                .templateStatus(AligoTemplateStatus.R)
                .status(EntityStatus.ACTIVE)
                .buttons(List.of())
                .build());
    }
}
