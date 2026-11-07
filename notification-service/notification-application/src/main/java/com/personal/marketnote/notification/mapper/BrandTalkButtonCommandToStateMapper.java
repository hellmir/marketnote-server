package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButton;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButtonCreateState;
import com.personal.marketnote.notification.port.in.command.BrandTalkButtonCommand;

import java.util.List;

public class BrandTalkButtonCommandToStateMapper {

    private BrandTalkButtonCommandToStateMapper() {
    }

    public static List<BrandTalkButton> mapButtons(List<BrandTalkButtonCommand> buttonCommands) {
        if (FormatValidator.hasNoValue(buttonCommands)) {
            return List.of();
        }
        return buttonCommands.stream()
                .map(BrandTalkButtonCommandToStateMapper::mapButton)
                .toList();
    }

    static BrandTalkButton mapButton(BrandTalkButtonCommand btn) {
        return BrandTalkButton.from(
                BrandTalkButtonCreateState.builder()
                        .name(btn.name())
                        .linkType(AligoTemplateButtonLinkType.from(btn.linkType()))
                        .linkM(btn.linkM())
                        .linkP(btn.linkP())
                        .linkI(btn.linkI())
                        .linkA(btn.linkA())
                        .build()
        );
    }
}
