package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientStatus;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageRecipientListResult;

import java.time.LocalDateTime;
import java.util.List;

public record GetAligoMessageRecipientListResponse(
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

    public static GetAligoMessageRecipientListResponse from(GetAligoMessageRecipientListResult result) {
        List<RecipientDetail> details = result.recipients().stream()
                .map(r -> new RecipientDetail(
                        r.id(), r.receiver(), r.recvName(), r.msgId(), r.smid(),
                        r.recipientStatus(), r.rslt(), r.rsltMessage(),
                        r.sentAt(), r.resultAt(), r.reportAt()
                ))
                .toList();
        return new GetAligoMessageRecipientListResponse(result.messageId(), details);
    }
}
