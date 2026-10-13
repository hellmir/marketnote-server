package com.personal.marketnote.notification.port.in.result.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientStatus;

import java.time.LocalDateTime;
import java.util.List;

public record GetAligoMessageRecipientListResult(
        Long messageId,
        List<RecipientDetail> recipients
) {

    public record RecipientDetail(
            Long id,
            String receiver,
            String recvName,
            String msgId,
            String smid,
            AligoMessageRecipientStatus recipientStatus,
            String rslt,
            String rsltMessage,
            LocalDateTime sentAt,
            LocalDateTime resultAt,
            LocalDateTime reportAt
    ) {
    }
}
