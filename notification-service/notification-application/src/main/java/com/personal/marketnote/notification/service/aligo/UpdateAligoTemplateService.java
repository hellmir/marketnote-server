package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.AligoTemplateButtonCommand;
import com.personal.marketnote.notification.port.in.command.UpdateAligoTemplateCommand;
import com.personal.marketnote.notification.port.in.result.aligo.UpdateAligoTemplateResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.UpdateAligoTemplateUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoTemplatePort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplateExternalPort;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoTemplatePort;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class UpdateAligoTemplateService implements UpdateAligoTemplateUseCase {

    private final FindAligoTemplatePort findAligoTemplatePort;
    private final UpdateAligoTemplateExternalPort updateAligoTemplateExternalPort;
    private final UpdateAligoTemplatePort updateAligoTemplatePort;

    @Override
    public UpdateAligoTemplateResult updateTemplate(UpdateAligoTemplateCommand command) {
        AligoTemplate template = findAligoTemplatePort.findByTemplateCode(command.templateCode())
                .orElseThrow(() -> new AligoTemplateNotFoundException(command.templateCode()));

        List<AligoTemplateButton> buttons = mapButtons(command.buttons());
        template.updateContent(
                command.templateName(), command.templateContent(),
                command.templateTitle(), command.templateSubtitle(), buttons
        );

        updateAligoTemplateExternalPort.updateTemplate(template);

        updateAligoTemplatePort.update(template);

        return new UpdateAligoTemplateResult(template.getId(), template.getTemplateCode(), template.getTemplateName());
    }

    private List<AligoTemplateButton> mapButtons(List<AligoTemplateButtonCommand> buttonCommands) {
        if (FormatValidator.hasNoValue(buttonCommands)) {
            return List.of();
        }
        return buttonCommands.stream()
                .map(btn -> AligoTemplateButton.builder()
                        .ordering(btn.ordering())
                        .name(btn.name())
                        .linkType(AligoTemplateButtonLinkType.from(btn.linkType()))
                        .linkMo(btn.linkMo())
                        .linkPc(btn.linkPc())
                        .linkIos(btn.linkIos())
                        .linkAnd(btn.linkAnd())
                        .build()
                )
                .toList();
    }
}
