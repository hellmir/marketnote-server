package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.exception.QuickPaymentBatchKeyDeletionFailedException;
import com.personal.marketnote.commerce.exception.QuickPaymentCardNotFoundException;
import com.personal.marketnote.commerce.port.in.command.quickpayment.DeleteQuickPaymentCardCommand;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.DeleteQuickPaymentCardUseCase;
import com.personal.marketnote.commerce.port.out.quickpayment.*;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class DeleteQuickPaymentCardService implements DeleteQuickPaymentCardUseCase {
    private final FindQuickPaymentCardPort findQuickPaymentCardPort;
    private final DeleteBatchKeyPort deleteBatchKeyPort;
    private final DeleteQuickPaymentCardPort deleteQuickPaymentCardPort;

    @Override
    public void delete(DeleteQuickPaymentCardCommand command) {
        log.info("빠른결제 카드 삭제 요청 - userId: {}, cardId: {}", command.userId(), command.quickPaymentCardId());

        QuickPaymentCard card = findQuickPaymentCardPort
                .findActiveByIdAndUserId(command.quickPaymentCardId(), command.userId())
                .orElseThrow(() -> new QuickPaymentCardNotFoundException(
                        command.quickPaymentCardId(), command.userId()));

        DeleteBatchKeyPortResult portResult = deleteBatchKeyPort.deleteBatchKey(
                DeleteBatchKeyPortCommand.builder()
                        .batchKey(card.getBatchKey())
                        .groupId(card.getGroupId())
                        .build()
        );

        if (!portResult.success()) {
            throw new QuickPaymentBatchKeyDeletionFailedException(portResult.resultCode(), portResult.resultMessage());
        }

        card.deactivate();
        deleteQuickPaymentCardPort.deactivate(card);
    }
}
