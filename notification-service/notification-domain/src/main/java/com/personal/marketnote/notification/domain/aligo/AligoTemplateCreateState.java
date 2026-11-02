package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoTemplateCreateState {

    private static final int MAX_BUTTON_COUNT = 5;

    private String senderKey;
    private String templateCode;
    private String templateName;
    private String templateContent;
    private String templateTitle;
    private String templateSubtitle;
    private AligoTemplateType templateType;
    private AligoTemplateEmType templateEmType;
    private boolean templateSecure;
    private boolean templateAdvert;
    private String imageUrl;
    private String imageName;
    private List<AligoTemplateButton> buttons;

    public boolean hasTooManyButtons() {
        return FormatValidator.hasValue(buttons) && buttons.size() > MAX_BUTTON_COUNT;
    }
}
