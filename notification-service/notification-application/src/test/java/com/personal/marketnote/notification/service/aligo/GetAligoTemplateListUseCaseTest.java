package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoTemplateListResult;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplateListPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoTemplateListExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAligoTemplateListUseCaseTest {

    @InjectMocks
    private GetAligoTemplateListService getAligoTemplateListService;

    @Mock
    private GetAligoTemplateListExternalPort getAligoTemplateListExternalPort;

    @Mock
    private FindAligoTemplateListPort findAligoTemplateListPort;

    @Mock
    private SaveAligoTemplatePort saveAligoTemplatePort;

    @Mock
    private UpdateAligoTemplatePort updateAligoTemplatePort;

    @Test
    @DisplayName("외부 API에서 조회한 신규 템플릿을 로컬 DB에 저장한다")
    void shouldSaveNewTemplateFromExternalApi() {
        // given
        String senderKey = "sender-key-1";
        AligoTemplate externalTemplate = createTemplate(null, "TPL001", "REG", AligoTemplateStatus.R);

        when(getAligoTemplateListExternalPort.getTemplateList(senderKey)).thenReturn(List.of(externalTemplate));
        when(findAligoTemplateListPort.findAllBySenderKey(senderKey)).thenReturn(List.of());
        when(saveAligoTemplatePort.save(any(AligoTemplate.class))).thenReturn(externalTemplate);

        // when
        GetAligoTemplateListResult result = getAligoTemplateListService.getTemplateList(senderKey);

        // then
        assertThat(result.templates()).hasSize(1);
        assertThat(result.templates().get(0).templateCode()).isEqualTo("TPL001");
        assertThat(result.templates().get(0).inspStatus()).isEqualTo("REG");

        ArgumentCaptor<AligoTemplate> captor = ArgumentCaptor.forClass(AligoTemplate.class);
        verify(saveAligoTemplatePort).save(captor.capture());
        assertThat(captor.getValue().getTemplateCode()).isEqualTo("TPL001");
        verify(updateAligoTemplatePort, never()).update(any());
    }

    @Test
    @DisplayName("동기화 시 기존 레코드의 inspStatus를 갱신한다 (REG → APR)")
    void shouldUpdateExistingTemplateInspStatus() {
        // given
        String senderKey = "sender-key-1";
        AligoTemplate externalTemplate = createTemplate(null, "TPL001", "APR", AligoTemplateStatus.A);
        AligoTemplate localTemplate = createTemplate(1L, "TPL001", "REG", AligoTemplateStatus.R);

        assertThat(localTemplate.getInspStatus()).isEqualTo("REG");
        assertThat(localTemplate.getTemplateStatus()).isEqualTo(AligoTemplateStatus.R);

        when(getAligoTemplateListExternalPort.getTemplateList(senderKey)).thenReturn(List.of(externalTemplate));
        when(findAligoTemplateListPort.findAllBySenderKey(senderKey)).thenReturn(List.of(localTemplate));

        // when
        GetAligoTemplateListResult result = getAligoTemplateListService.getTemplateList(senderKey);

        // then
        assertThat(result.templates()).hasSize(1);
        assertThat(result.templates().get(0).inspStatus()).isEqualTo("APR");
        assertThat(result.templates().get(0).templateStatus()).isEqualTo("A");
        verify(updateAligoTemplatePort).update(localTemplate);
        verify(saveAligoTemplatePort, never()).save(any());
    }

    @Test
    @DisplayName("외부 API 응답에 신규와 기존 템플릿이 혼합된 경우 각각 save와 update를 수행한다")
    void shouldSaveNewAndUpdateExistingTemplates() {
        // given
        String senderKey = "sender-key-1";
        AligoTemplate externalNew = createTemplate(null, "TPL002", "REG", AligoTemplateStatus.R);
        AligoTemplate externalExisting = createTemplate(null, "TPL001", "APR", AligoTemplateStatus.A);
        AligoTemplate localExisting = createTemplate(1L, "TPL001", "REQ", AligoTemplateStatus.R);

        when(getAligoTemplateListExternalPort.getTemplateList(senderKey))
                .thenReturn(List.of(externalNew, externalExisting));
        when(findAligoTemplateListPort.findAllBySenderKey(senderKey)).thenReturn(List.of(localExisting));
        when(saveAligoTemplatePort.save(any(AligoTemplate.class))).thenReturn(externalNew);

        // when
        GetAligoTemplateListResult result = getAligoTemplateListService.getTemplateList(senderKey);

        // then
        assertThat(result.templates()).hasSize(2);
        assertThat(result.templates())
                .extracting(GetAligoTemplateListResult.AligoTemplateResult::templateCode)
                .containsExactly("TPL002", "TPL001");

        ArgumentCaptor<AligoTemplate> saveCaptor = ArgumentCaptor.forClass(AligoTemplate.class);
        verify(saveAligoTemplatePort).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue().getTemplateCode()).isEqualTo("TPL002");
        verify(updateAligoTemplatePort).update(localExisting);
    }

    @Test
    @DisplayName("외부 API 응답이 비어있으면 빈 결과를 반환하고 save/update를 수행하지 않는다")
    void shouldReturnEmptyResultWhenExternalApiReturnsEmpty() {
        // given
        String senderKey = "sender-key-1";
        when(getAligoTemplateListExternalPort.getTemplateList(senderKey)).thenReturn(List.of());
        when(findAligoTemplateListPort.findAllBySenderKey(senderKey)).thenReturn(List.of());

        // when
        GetAligoTemplateListResult result = getAligoTemplateListService.getTemplateList(senderKey);

        // then
        assertThat(result.templates()).isEmpty();
        verifyNoInteractions(saveAligoTemplatePort);
        verifyNoInteractions(updateAligoTemplatePort);
    }

    private AligoTemplate createTemplate(
            Long id, String templateCode, String inspStatus, AligoTemplateStatus templateStatus
    ) {
        return AligoTemplate.from(AligoTemplateSnapshotState.builder()
                .id(id)
                .senderKey("sender-key-1")
                .templateCode(templateCode)
                .templateName("테스트 템플릿 " + templateCode)
                .templateContent("안녕하세요 #{이름}님")
                .templateType(AligoTemplateType.BA)
                .templateEmType(AligoTemplateEmType.NONE)
                .inspStatus(inspStatus)
                .templateStatus(templateStatus)
                .status(EntityStatus.ACTIVE)
                .buttons(List.of())
                .build());
    }
}
