package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.domain.payment.MaskedCardNumber;
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

        IssueBatchKeyPortCommand portCommand = IssueBatchKeyPortCommand.from(command);

        IssueBatchKeyPortResult portResult = issueBatchKeyPort.issueBatchKey(portCommand);

        if (!portResult.success()) {
            throw new QuickPaymentBatchKeyIssuanceFailedException(portResult.resultCode(), portResult.resultMessage());
        }

        QuickPaymentCardCreateState createState = QuickPaymentCardCreateState.forIssued(
                command.userId(),
                portResult.batchKey(),
                portResult.cardCode(),
                portResult.cardName(),
                MaskedCardNumber.fromNullable(command.maskedCardNumber()),
                portResult.cardBinType01(),
                portResult.cardBinType02()
        );

        QuickPaymentCard savedCard = saveQuickPaymentCardPort.save(QuickPaymentCard.from(createState));

        return IssueBatchKeyResult.from(savedCard);
    }
}
