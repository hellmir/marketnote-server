package com.personal.marketnote.notification.port.out.aligo;

import java.time.LocalDateTime;
import java.util.List;

public interface GetAligoMessageDetailExternalPort {

    List<AligoRecipientDetailResult> getMessageDetail(String mid);

    record AligoRecipientDetailResult(
            String msgId,
            String receiver,
            String rslt,
            String rsltMessage,
            LocalDateTime sentAt,
            LocalDateTime resultAt,
            LocalDateTime reportAt
    ) {
    }
}
