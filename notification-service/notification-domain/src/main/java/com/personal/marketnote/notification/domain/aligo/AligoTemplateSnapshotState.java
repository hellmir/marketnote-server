package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoTemplateSnapshotState {

    private Long id;
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
    private String inspStatus;
    private AligoTemplateStatus templateStatus;
    private String comments;
    private List<AligoTemplateButton> buttons;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
