package com.personal.marketnote.notification.service.aligo;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.AligoMessage;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientStatus;
import com.personal.marketnote.notification.port.in.usecase.aligo.PollAligoMessageDetailUseCase;
import com.personal.marketnote.notification.port.out.aligo.FindPendingAligoMessagesPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoMessageDetailExternalPort;
import com.personal.marketnote.notification.port.out.aligo.GetAligoMessageDetailExternalPort.AligoRecipientDetailResult;
import com.personal.marketnote.notification.port.out.aligo.UpdateAligoMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class PollAligoMessageDetailService implements PollAligoMessageDetailUseCase {

    private static final long POLLING_WINDOW_HOURS = 24;
    private static final String ALIGO_QUEUE_PREFIX = "Q";
    private static final String SUCCESS_RESULT_CODE = "0";

    private final FindPendingAligoMessagesPort findPendingAligoMessagesPort;
    private final GetAligoMessageDetailExternalPort getAligoMessageDetailExternalPort;
    private final UpdateAligoMessagePort updateAligoMessagePort;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    @Override
    public int poll() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime cutoff = now.minusHours(POLLING_WINDOW_HOURS);
        List<AligoMessage> pendingMessages = findPendingAligoMessagesPort.findPendingMessages(cutoff);

        int processedCount = 0;
        for (AligoMessage message : pendingMessages) {
            if (processMessage(message, now)) {
                processedCount++;
            }
        }
        return processedCount;
    }

    private boolean processMessage(AligoMessage message, LocalDateTime now) {
        try {
            List<AligoRecipientDetailResult> details =
                    getAligoMessageDetailExternalPort.getMessageDetail(message.getAligoMid());

            transactionTemplate.execute(status -> {
                applyRecipientResults(message, details);
                forceResolveIfExpired(message, now);
                message.resolveAfterPolling();
                updateAligoMessagePort.update(message);
                return null;
            });
            return true;
        } catch (Exception e) {
            log.warn("알리고 메시지 폴링 실패: messageId={}, aligoMid={}",
                    message.getId(), message.getAligoMid(), e);
            return false;
        }
    }

    private void applyRecipientResults(AligoMessage message, List<AligoRecipientDetailResult> details) {
        for (AligoRecipientDetailResult detail : details) {
            if (isQueuedMessage(detail.msgId())) {
                continue;
            }
            AligoMessageRecipientStatus targetStatus = resolveRecipientStatus(detail.rslt());
            message.updateRecipientResult(
                    detail.receiver(), targetStatus, detail.msgId(),
                    detail.rslt(), detail.rsltMessage(),
                    detail.sentAt(), detail.resultAt(), detail.reportAt()
            );
        }
    }

    private boolean isQueuedMessage(String msgId) {
        return FormatValidator.hasValue(msgId) && msgId.startsWith(ALIGO_QUEUE_PREFIX);
    }

    private AligoMessageRecipientStatus resolveRecipientStatus(String rslt) {
        if (SUCCESS_RESULT_CODE.equals(rslt)) {
            return AligoMessageRecipientStatus.SUCCESS;
        }
        return AligoMessageRecipientStatus.FAILED;
    }

    private void forceResolveIfExpired(AligoMessage message, LocalDateTime now) {
        if (FormatValidator.hasNoValue(message.getCreatedAt())) {
            return;
        }
        if (message.getCreatedAt().plusHours(POLLING_WINDOW_HOURS).isBefore(now)) {
            message.forceResolveExpiredRecipients();
        }
    }
}
