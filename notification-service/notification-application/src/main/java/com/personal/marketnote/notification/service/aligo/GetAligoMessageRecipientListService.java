package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.domain.aligo.AligoMessageNotFoundException;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipient;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageRecipientListResult;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageRecipientListResult.RecipientDetail;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoMessageRecipientListUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindAligoMessagePort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetAligoMessageRecipientListService implements GetAligoMessageRecipientListUseCase {

    private final FindAligoMessagePort findAligoMessagePort;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, readOnly = true)
    public GetAligoMessageRecipientListResult getRecipientList(Long messageId) {
        AligoMessage message = findAligoMessagePort.findById(messageId)
                .orElseThrow(() -> new AligoMessageNotFoundException(messageId));

        List<RecipientDetail> recipients = message.getRecipients().stream()
                .map(this::toRecipientDetail)
                .toList();

        return new GetAligoMessageRecipientListResult(messageId, recipients);
    }

    private RecipientDetail toRecipientDetail(AligoMessageRecipient recipient) {
        return new RecipientDetail(
                recipient.getId(),
                recipient.getReceiver(),
                recipient.getRecvName(),
                recipient.getMsgId(),
                recipient.getSmid(),
                recipient.getRecipientStatus(),
                recipient.getRslt(),
                recipient.getRsltMessage(),
                recipient.getSentAt(),
                recipient.getResultAt(),
                recipient.getReportAt()
        );
    }
}
