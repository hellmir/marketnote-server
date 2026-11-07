package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.aligo.AligoTemplateButton;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateEmType;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateType;
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
}
