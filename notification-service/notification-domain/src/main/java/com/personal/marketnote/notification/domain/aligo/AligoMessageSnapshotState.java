package com.personal.marketnote.notification.domain.aligo;

import com.personal.marketnote.common.domain.EntityStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class AligoMessageSnapshotState {

    private Long id;
    private String senderKey;
    private String templateCode;
    private String sender;
    private AligoMessageType messageType;
    private String content;
    private String title;
    private String buttons;
    private AligoMessageStatus messageStatus;
    private String aligoMid;
    private int successCount;
    private int failCount;
    private boolean failoverUsed;
    private String failoverSubject;
    private String failoverMessageBody;
    private LocalDateTime requestedAt;
    private LocalDateTime sendDate;
    private LocalDateTime sentAt;
    private List<AligoMessageRecipientSnapshotState> recipients;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
