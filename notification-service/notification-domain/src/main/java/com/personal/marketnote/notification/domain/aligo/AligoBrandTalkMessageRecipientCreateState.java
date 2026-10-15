package com.personal.marketnote.notification.domain.aligo;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AligoBrandTalkMessageRecipientCreateState {

    private String receiver;
    private String receiverMessage;
}
