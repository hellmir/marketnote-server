package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AligoBrandTalkMessageRecipientSnapshotState {

    private Long id;
    private String receiver;
    private String receiverMessage;
    private String msgId;
    private AligoMessageRecipientStatus recipientStatus;
    private String rslt;
    private String rsltMessage;
}
