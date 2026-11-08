package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AligoMessageSnapshotState {

    private final Long id;
    private final String senderKey;
    private final String templateCode;
    private final String sender;
    private final AligoMessageType messageType;
    private final String content;
    private final String title;
    private final String buttons;
    private final AligoMessageStatus messageStatus;
    private final String aligoMid;
    private final int successCount;
    private final int failCount;
    private final boolean failoverUsed;
    private final String failoverSubject;
    private final String failoverMessageBody;
    private final LocalDateTime requestedAt;
    private final LocalDateTime sendDate;
    private final LocalDateTime sentAt;
    private final List<AligoMessageRecipientSnapshotState> recipients;
    private final EntityStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
}
