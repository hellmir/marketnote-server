package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.mapper.AligoTemplateCommandToStateMapper;
import com.personal.marketnote.notification.port.in.command.RegisterAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.RegisterAligoTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.RegisterAligoTemplateUseCase;
import com.personal.marketnote.notification.port.out.aligo.RegisterAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.SaveAligoTemplatePort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class RegisterAligoTemplateService implements RegisterAligoTemplateUseCase {

    private final RegisterAligoTemplateExternalPort registerAligoTemplateExternalPort;
    private final SaveAligoTemplatePort saveAligoTemplatePort;

    @Override
    public RegisterAligoTemplateResult registerTemplate(RegisterAligoTemplateCommand command) {
        AligoTemplate template = createTemplate(command);

        registerAligoTemplateExternalPort.registerTemplate(template);

        AligoTemplate saved = saveAligoTemplatePort.save(template);

        return new RegisterAligoTemplateResult(saved.getId(), saved.getTemplateCode(), saved.getTemplateName());
    }

    private AligoTemplate createTemplate(RegisterAligoTemplateCommand command) {
        List<AligoTemplateButton> buttons = AligoTemplateCommandToStateMapper.mapButtons(command.buttons());

        return AligoTemplate.from(AligoTemplateCommandToStateMapper.mapToState(command, buttons));
    }
}
