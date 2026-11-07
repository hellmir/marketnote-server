package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateEmType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateType;
import com.personal.marketnote.notification.port.in.command.AligoTemplateButtonCommand;
import com.personal.marketnote.notification.port.in.command.RegisterAligoTemplateCommand;

import java.util.List;

public class AligoTemplateCommandToStateMapper {

    private AligoTemplateCommandToStateMapper() {
    }

    public static AligoTemplateCreateState mapToState(
            RegisterAligoTemplateCommand command, List<AligoTemplateButton> buttons
    ) {
        return AligoTemplateCreateState.builder()
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
                .build();
    }

    public static List<AligoTemplateButton> mapButtons(List<AligoTemplateButtonCommand> buttonCommands) {
        if (FormatValidator.hasNoValue(buttonCommands)) {
            return List.of();
        }
        return buttonCommands.stream()
                .map(AligoTemplateCommandToStateMapper::mapButton)
                .toList();
    }

    private static AligoTemplateButton mapButton(AligoTemplateButtonCommand btn) {
        return AligoTemplateButton.from(
                AligoTemplateButtonCreateState.builder()
                        .ordering(btn.ordering())
                        .name(btn.name())
                        .linkType(AligoTemplateButtonLinkType.from(btn.linkType()))
                        .linkMo(btn.linkMo())
                        .linkPc(btn.linkPc())
                        .linkIos(btn.linkIos())
                        .linkAnd(btn.linkAnd())
                        .build()
        );
    }
}
