package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateEmType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateType;
import com.personal.marketnote.notification.port.in.command.AligoTemplateButtonCommand;
import com.personal.marketnote.notification.port.in.command.RegisterAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoTemplateResult;
import com.personal.marketnote.notification.port.out.aligo.RegisterAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoTemplatePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAligoTemplateUseCaseTest {

    @InjectMocks
    private RegisterAligoTemplateService registerAligoTemplateService;

    @Mock
    private RegisterAligoTemplateExternalPort registerAligoTemplateExternalPort;

    @Mock
    private SaveAligoTemplatePort saveAligoTemplatePort;

    @Test
    @DisplayName("템플릿을 등록하면 외부 API 호출 후 로컬 DB에 저장한다")
    void shouldRegisterTemplateViaExternalApiAndSaveLocally() {
        // given
        RegisterAligoTemplateCommand command = new RegisterAligoTemplateCommand(
                "sender-key", "TPL001", "테스트 템플릿", "안녕하세요",
                null, null, "BA", "NONE", false, false, null, null,
                List.of(new AligoTemplateButtonCommand(1, "확인", "WL", "https://m.example.com", null, null, null))
        );

        when(saveAligoTemplatePort.save(any(AligoTemplate.class))).thenAnswer(invocation -> {
            AligoTemplate template = invocation.getArgument(0);
            return template;
        });

        // when
        RegisterAligoTemplateResult result = registerAligoTemplateService.registerTemplate(command);

        // then
        assertThat(result.templateCode()).isEqualTo("TPL001");
        assertThat(result.templateName()).isEqualTo("테스트 템플릿");
        verify(registerAligoTemplateExternalPort).registerTemplate(any(AligoTemplate.class));
        verify(saveAligoTemplatePort).save(any(AligoTemplate.class));
    }
}
