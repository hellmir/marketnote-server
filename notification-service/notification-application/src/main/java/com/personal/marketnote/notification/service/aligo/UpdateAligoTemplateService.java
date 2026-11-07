package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateNotFoundException;
import com.personal.marketnote.notification.mapper.AligoTemplateCommandToStateMapper;
import com.personal.marketnote.notification.port.in.command.UpdateAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.UpdateAligoTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.UpdateAligoTemplateUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UpdateAligoTemplateService implements UpdateAligoTemplateUseCase {

    private final FindAligoTemplatePort findAligoTemplatePort;
    private final UpdateAligoTemplateExternalPort updateAligoTemplateExternalPort;
    private final UpdateAligoTemplatePort updateAligoTemplatePort;

    @Override
    public UpdateAligoTemplateResult updateTemplate(UpdateAligoTemplateCommand command) {
        AligoTemplate template = findAligoTemplatePort.findByTemplateCode(command.templateCode())
                .orElseThrow(() -> {
                    log.warn("알림톡 템플릿을 찾을 수 없습니다. templateCode={}", command.templateCode());
                    return new AligoTemplateNotFoundException();
                });

        List<AligoTemplateButton> buttons = AligoTemplateCommandToStateMapper.mapButtons(command.buttons());
        template.updateContent(
                command.templateName(), command.templateContent(),
                command.templateTitle(), command.templateSubtitle(), buttons
        );

        updateAligoTemplateExternalPort.updateTemplate(template);

        updateAligoTemplatePort.update(template);

        return new UpdateAligoTemplateResult(template.getId(), template.getTemplateCode(), template.getTemplateName());
    }
}
