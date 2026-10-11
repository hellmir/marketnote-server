package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoMessage;

public interface SendAligoAlimTalkExternalPort {

    AligoAlimTalkSendResponse send(AligoMessage message);

    record AligoAlimTalkSendResponse(String mid, int successCount, int failCount) {
    }
}
