package com.personal.marketnote.commerce.adapter.in.web.payment.mapper;

import com.personal.marketnote.commerce.adapter.in.web.payment.request.ApprovePaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.request.CancelPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.request.ReadyPaymentRequest;
import com.personal.marketnote.commerce.port.in.command.payment.ApprovePaymentCommand;
import com.personal.marketnote.commerce.port.in.command.payment.CancelPaymentCommand;
import com.personal.marketnote.commerce.port.in.command.payment.ReadyPaymentCommand;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;

public class PaymentRequestToCommandMapper {
    private PaymentRequestToCommandMapper() {
    }

    public static ReadyPaymentCommand mapToCommand(ReadyPaymentRequest request, Long buyerId) {
        return ReadyPaymentCommand.builder()
                .buyerId(buyerId)
                .orderKey(request.getOrderKey())
                .payMethod(request.getPayMethod())
                .goodName(request.getGoodName())
                .build();
    }

    public static ApprovePaymentCommand mapToCommand(ApprovePaymentRequest request, Long buyerId) {
        return ApprovePaymentCommand.builder()
                .buyerId(buyerId)
                .orderKey(request.getOrderKey())
                .encData(request.getEncData())
                .encInfo(request.getEncInfo())
                .payType(request.getPayType())
                .build();
    }

    public static CancelPaymentCommand mapToCommand(String orderKey, CancelPaymentRequest request, Long buyerId) {
        List<CancelPaymentCommand.CancelProductItem> cancelProducts = FormatValidator.hasValue(request.getCancelProducts())
                ? request.getCancelProducts().stream()
                        .map(item -> new CancelPaymentCommand.CancelProductItem(
                                item.getPricePolicyId(), item.getQuantity()))
                        .toList()
                : null;

        return CancelPaymentCommand.builder()
                .buyerId(buyerId)
                .orderKey(orderKey)
                .cancelType(request.getCancelType())
                .cancelAmount(request.getCancelAmount())
                .cancelReason(request.getCancelReason())
                .cancelProducts(cancelProducts)
                .build();
    }
}
