package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AligoBrandTalkMessageRecipientSnapshotState {

    private final Long id;
    private final String receiver;
    private final String receiverMessage;
    private final String msgId;
    private final AligoMessageRecipientStatus recipientStatus;
    private final String rslt;
    private final String rsltMessage;
}
