package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.exception.PaymentApprovalException;
import com.personal.marketnote.commerce.exception.PaymentNotFoundException;
import com.personal.marketnote.commerce.port.in.command.payment.ReadyPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.ReadyPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.ReadyPaymentUseCase;
import com.personal.marketnote.commerce.port.out.payment.FindPaymentPort;
import com.personal.marketnote.commerce.port.out.payment.PaymentVendorPort;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorCommand;
import com.personal.marketnote.commerce.port.out.payment.vendor.TradeRegisterVendorResult;
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
public class ReadyPaymentService implements ReadyPaymentUseCase {
    private final FindPaymentPort findPaymentPort;
    private final PaymentVendorPort paymentVendorPort;

    @Override
    public ReadyPaymentResult ready(ReadyPaymentCommand command) {
        UUID orderKeyUuid = UUID.fromString(command.orderKey());

        Payment payment = findPaymentPort.findByOrderKey(orderKeyUuid)
                .orElseThrow(() -> new PaymentNotFoundException(command.orderKey()));

        TradeRegisterVendorCommand vendorCommand = TradeRegisterVendorCommand.builder()
                .orderKey(payment.getOrderKey().toString())
                .goodMny(String.valueOf(payment.getPaymentAmount()))
                .payMethod(command.payMethod())
                .goodName(command.goodName())
                .build();

        TradeRegisterVendorResult vendorResult = paymentVendorPort.registerTrade(vendorCommand);

        if (!vendorResult.isSuccess()) {
            throw PaymentApprovalException.kcpTradeRegisterFailed(vendorResult.resCd(), vendorResult.resMsg());
        }

        return ReadyPaymentResult.builder()
                .orderKey(payment.getOrderKey().toString())
                .approvalKey(vendorResult.approvalKey())
                .payUrl(vendorResult.payUrl())
                .traceNo(vendorResult.traceNo())
                .build();
    }
}
