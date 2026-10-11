package com.personal.marketnote.notification.domain.aligo;

import lombok.*;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoTemplateCreateState {

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
}
