package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AligoMessageRecipientSnapshotState {

    private final Long id;
    private final String receiver;
    private final String recvName;
    private final String msgId;
    private final String smid;
    private final AligoMessageRecipientStatus recipientStatus;
    private final String rslt;
    private final String rsltMessage;
    private final LocalDateTime sentAt;
    private final LocalDateTime resultAt;
    private final LocalDateTime reportAt;
}
