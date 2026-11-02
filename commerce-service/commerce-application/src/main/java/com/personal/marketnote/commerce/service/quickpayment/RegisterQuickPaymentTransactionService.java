package com.personal.marketnote.commerce.service.quickpayment;

import com.personal.marketnote.commerce.exception.QuickPaymentTransactionFailedException;
import com.personal.marketnote.commerce.port.in.command.quickpayment.RegisterQuickPaymentTransactionCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.RegisterQuickPaymentTransactionResult;
import com.personal.marketnote.commerce.port.in.usecase.quickpayment.RegisterQuickPaymentTransactionUseCase;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPort;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPortCommand;
import com.personal.marketnote.commerce.port.out.quickpayment.RegisterQuickPaymentTransactionPortResult;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class RegisterQuickPaymentTransactionService implements RegisterQuickPaymentTransactionUseCase {
    private final RegisterQuickPaymentTransactionPort registerQuickPaymentTransactionPort;

    @Override
    public RegisterQuickPaymentTransactionResult register(RegisterQuickPaymentTransactionCommand command) {
        String transactionId = UUID.randomUUID().toString();
        log.info("빠른결제 거래등록 요청 - userId: {}, transactionId: {}", command.userId(), transactionId);

        RegisterQuickPaymentTransactionPortCommand portCommand = RegisterQuickPaymentTransactionPortCommand.builder()
                .transactionId(transactionId)
                .build();

        RegisterQuickPaymentTransactionPortResult portResult =
                registerQuickPaymentTransactionPort.registerTransaction(portCommand);

        if (!portResult.success()) {
            throw new QuickPaymentTransactionFailedException(portResult.resultCode(), portResult.resultMessage());
        }

        return RegisterQuickPaymentTransactionResult.builder()
                .transactionId(transactionId)
                .approvalKey(portResult.approvalKey())
                .payUrl(portResult.payUrl())
                .traceNo(portResult.traceNo())
                .build();
    }
}
