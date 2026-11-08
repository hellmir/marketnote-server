package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AligoTemplateSnapshotState {

    private final Long id;
    private final String senderKey;
    private final String templateCode;
    private final String templateName;
    private final String templateContent;
    private final String templateTitle;
    private final String templateSubtitle;
    private final AligoTemplateType templateType;
    private final AligoTemplateEmType templateEmType;
    private final boolean templateSecure;
    private final boolean templateAdvert;
    private final String imageUrl;
    private final String imageName;
    private final String inspStatus;
    private final AligoTemplateStatus templateStatus;
    private final String comments;
    private final List<AligoTemplateButton> buttons;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
