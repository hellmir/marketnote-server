package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.*;
import com.personal.marketnote.notification.port.in.command.AligoTemplateButtonCommand;
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
        List<AligoTemplateButton> buttons = mapButtons(command.buttons());

        return AligoTemplate.from(
                AligoTemplateCreateState.builder()
                        .senderKey(command.senderKey())
                        .templateCode(command.templateCode())
                        .templateName(command.templateName())
                        .templateContent(command.templateContent())
                        .templateTitle(command.templateTitle())
                        .templateSubtitle(command.templateSubtitle())
                        .templateType(AligoTemplateType.from(command.templateType()))
                        .templateEmType(AligoTemplateEmType.from(command.templateEmType()))
                        .templateSecure(command.templateSecure())
                        .templateAdvert(command.templateAdvert())
                        .imageUrl(command.imageUrl())
                        .imageName(command.imageName())
                        .buttons(buttons)
                        .build()
        );
    }

    private List<AligoTemplateButton> mapButtons(List<AligoTemplateButtonCommand> buttonCommands) {
        if (buttonCommands == null) {
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
