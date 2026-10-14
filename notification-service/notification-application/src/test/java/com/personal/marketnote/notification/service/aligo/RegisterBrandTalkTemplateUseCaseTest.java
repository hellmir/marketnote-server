package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateType;
import com.personal.marketnote.notification.port.in.command.BrandTalkButtonCommand;
import com.personal.marketnote.notification.port.in.command.RegisterBrandTalkTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterBrandTalkTemplateResult;
import com.personal.marketnote.notification.port.out.aligo.RegisterBrandTalkTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveBrandTalkTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterBrandTalkTemplateUseCaseTest {

    @InjectMocks
    private RegisterBrandTalkTemplateService registerBrandTalkTemplateService;

    @Mock
    private RegisterBrandTalkTemplateExternalPort registerBrandTalkTemplateExternalPort;

    @Mock
    private SaveBrandTalkTemplatePort saveBrandTalkTemplatePort;

    @Test
    @DisplayName("TEXT 타입 브랜드메시지 템플릿을 정상 등록하고 로컬에 저장한다")
    void shouldRegisterTextTemplateAndSaveLocally() {
        // given
        RegisterBrandTalkTemplateCommand command = new RegisterBrandTalkTemplateCommand(
                "sender-key", "BT_TEXT_001", "텍스트 템플릿", "TEXT",
                "안녕하세요", false, null, List.of(), null, null, null, List.of(), null, null
        );

        when(saveBrandTalkTemplatePort.save(any(AligoBrandTalkTemplate.class)))
                .thenAnswer(invocation -> {
                    AligoBrandTalkTemplate template = invocation.getArgument(0);
                    return AligoBrandTalkTemplate.from(
                            com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateSnapshotState.builder()
                                    .id(1L)
                                    .senderKey(template.getSenderKey())
                                    .templateCode(template.getTemplateCode())
                                    .templateName(template.getTemplateName())
                                    .templateType(template.getTemplateType())
                                    .templateContent(template.getTemplateContent())
                                    .buttons(List.of())
                                    .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                                    .build()
                    );
                });

        // when
        RegisterBrandTalkTemplateResult result = registerBrandTalkTemplateService.registerTemplate(command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.templateCode()).isEqualTo("BT_TEXT_001");
        assertThat(result.templateType()).isEqualTo("TEXT");
        verify(registerBrandTalkTemplateExternalPort).registerTemplate(any(), eq(null), eq(null));
        verify(saveBrandTalkTemplatePort).save(any());
    }

    @Test
    @DisplayName("IMAGE 타입 브랜드메시지 템플릿을 이미지와 함께 정상 등록한다")
    void shouldRegisterImageTemplateWithImage() {
        // given
        byte[] imageFile = new byte[]{1, 2, 3};
        RegisterBrandTalkTemplateCommand command = new RegisterBrandTalkTemplateCommand(
                "sender-key", "BT_IMAGE_001", "이미지 템플릿", "IMAGE",
                "이미지 내용", false, "https://example.com",
                List.of(), null, null, null, List.of(), imageFile, "test.jpg"
        );

        when(saveBrandTalkTemplatePort.save(any(AligoBrandTalkTemplate.class)))
                .thenAnswer(invocation -> {
                    AligoBrandTalkTemplate template = invocation.getArgument(0);
                    return AligoBrandTalkTemplate.from(
                            com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateSnapshotState.builder()
                                    .id(2L)
                                    .senderKey(template.getSenderKey())
                                    .templateCode(template.getTemplateCode())
                                    .templateName(template.getTemplateName())
                                    .templateType(template.getTemplateType())
                                    .templateContent(template.getTemplateContent())
                                    .imageUrl("https://uploaded.com/test.jpg")
                                    .buttons(List.of())
                                    .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                                    .build()
                    );
                });

        // when
        RegisterBrandTalkTemplateResult result = registerBrandTalkTemplateService.registerTemplate(command);

        // then
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.templateType()).isEqualTo("IMAGE");
        verify(registerBrandTalkTemplateExternalPort).registerTemplate(any(), eq(imageFile), eq("test.jpg"));
        verify(saveBrandTalkTemplatePort).save(any());
    }

    @Test
    @DisplayName("WIDE 타입 브랜드메시지 템플릿을 이미지와 함께 정상 등록한다")
    void shouldRegisterWideTemplateWithImage() {
        // given
        byte[] imageFile = new byte[]{4, 5, 6};
        RegisterBrandTalkTemplateCommand command = new RegisterBrandTalkTemplateCommand(
                "sender-key", "BT_WIDE_001", "와이드 템플릿", "WIDE",
                "와이드 내용", false, "https://example.com",
                List.of(new BrandTalkButtonCommand("버튼", "WL", "https://m.ex.com", "https://www.ex.com", null, null)),
                null, null, null, List.of(), imageFile, "wide.jpg"
        );

        when(saveBrandTalkTemplatePort.save(any(AligoBrandTalkTemplate.class)))
                .thenAnswer(invocation -> {
                    AligoBrandTalkTemplate template = invocation.getArgument(0);
                    return AligoBrandTalkTemplate.from(
                            com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateSnapshotState.builder()
                                    .id(3L)
                                    .senderKey(template.getSenderKey())
                                    .templateCode(template.getTemplateCode())
                                    .templateName(template.getTemplateName())
                                    .templateType(template.getTemplateType())
                                    .templateContent(template.getTemplateContent())
                                    .buttons(template.getButtons())
                                    .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                                    .build()
                    );
                });

        // when
        RegisterBrandTalkTemplateResult result = registerBrandTalkTemplateService.registerTemplate(command);

        // then
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.templateType()).isEqualTo("WIDE");
        verify(registerBrandTalkTemplateExternalPort).registerTemplate(any(), eq(imageFile), eq("wide.jpg"));
    }

    @Test
    @DisplayName("외부 API 실패 시 로컬 저장을 수행하지 않는다")
    void shouldNotSaveLocallyWhenExternalApiFails() {
        // given
        RegisterBrandTalkTemplateCommand command = new RegisterBrandTalkTemplateCommand(
                "sender-key", "BT_TEXT_002", "실패 템플릿", "TEXT",
                "내용", false, null, List.of(), null, null, null, List.of(), null, null
        );

        doThrow(new RuntimeException("API 실패"))
                .when(registerBrandTalkTemplateExternalPort).registerTemplate(any(), any(), any());

        // when & then
        assertThatThrownBy(() -> registerBrandTalkTemplateService.registerTemplate(command))
                .isInstanceOf(RuntimeException.class);
        verifyNoInteractions(saveBrandTalkTemplatePort);
    }
}
