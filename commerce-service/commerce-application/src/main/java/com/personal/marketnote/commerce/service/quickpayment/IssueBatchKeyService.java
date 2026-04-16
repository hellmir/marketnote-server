package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCardCreateState;
import com.personal.marketnote.commerce.exception.QuickPaymentBatchKeyIssuanceFailedException;
import com.personal.marketnote.commerce.port.in.command.quickpayment.IssueBatchKeyCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.IssueBatchKeyResult;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.IssueBatchKeyUseCase;
import com.personal.marketnote.commerce.port.out.quickpayment.IssueBatchKeyPort;
import com.personal.marketnote.commerce.port.out.quickpayment.IssueBatchKeyPortCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.IssueBatchKeyPortResult;
import com.personal.marketnote.commerce.port.out.quickpayment.SaveQuickPaymentCardPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class IssueBatchKeyService implements IssueBatchKeyUseCase {
    private final IssueBatchKeyPort issueBatchKeyPort;
    private final SaveQuickPaymentCardPort saveQuickPaymentCardPort;

    @Override
    public IssueBatchKeyResult issueBatchKey(IssueBatchKeyCommand command) {
        log.info("빠른결제 배치키 발급 요청 - userId: {}", command.userId());

        IssueBatchKeyPortCommand portCommand = IssueBatchKeyPortCommand.builder()
                .encData(command.encData())
                .encInfo(command.encInfo())
                .build();

        IssueBatchKeyPortResult portResult = issueBatchKeyPort.issueBatchKey(portCommand);

        if (!portResult.success()) {
            throw new QuickPaymentBatchKeyIssuanceFailedException(portResult.resultCode(), portResult.resultMessage());
        }

        QuickPaymentCardCreateState createState = QuickPaymentCardCreateState.builder()
                .userId(command.userId())
                .batchKey(portResult.batchKey())
                .cardCode(portResult.cardCode())
                .cardName(portResult.cardName())
                .cardBinType01(portResult.cardBinType01())
                .cardBinType02(portResult.cardBinType02())
                .build();

        QuickPaymentCard savedCard = saveQuickPaymentCardPort.save(QuickPaymentCard.from(createState));

        return IssueBatchKeyResult.builder()
                .quickPaymentCardId(savedCard.getId())
                .cardCode(savedCard.getCardCode())
                .cardName(savedCard.getCardName())
                .maskedCardNumber(savedCard.getMaskedCardNumber())
                .cardBinType01(savedCard.getCardBinType01())
                .cardBinType02(savedCard.getCardBinType02())
                .build();
    }
}
