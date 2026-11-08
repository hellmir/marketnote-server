package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AligoTemplateExternalState {

    private final String senderKey;
    private final String templateCode;
    private final String templateName;
    private final String templateContent;
    private final AligoTemplateType templateType;
    private final AligoTemplateEmType templateEmType;
    private final String inspStatus;
    private final AligoTemplateStatus templateStatus;
    private final String comments;
    private final List<AligoTemplateButton> buttons;
}
