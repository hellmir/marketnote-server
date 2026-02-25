package com.personal.marketnote.commerce.adapter.out.persistence.payment.mapper;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.entity.PspPaymentEventJpaEntity;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEventSnapshotState;

public class PspPaymentEventEntityToDomainMapper {

    public static PspPaymentEvent toDomain(PspPaymentEventJpaEntity entity) {
        return PspPaymentEvent.from(
                PspPaymentEventSnapshotState.builder()
                        .id(entity.getId())
                        .orderId(entity.getOrderId())
                        .orderKey(entity.getOrderKey())
                        .pgCompanyKey(entity.getPgCompanyKey())
                        .pgShopKey(entity.getPgShopKey())
                        .pgPaymentKey(entity.getPgPaymentKey())
                        .pgApprovalResult(entity.getPgApprovalResult())
                        .pgCancelApprovalResult(entity.getPgCancelApprovalResult())
                        .shopTransactionId(entity.getShopTransactionId())
                        .poStatus(entity.getPoStatus())
                        .method(entity.getMethod())
                        .amount(entity.getAmount())
                        .vatAmount(entity.getVatAmount())
                        .natAmount(entity.getNatAmount())
                        .cardNumber(entity.getCardNumber())
                        .approvalNumber(entity.getApprovalNumber())
                        .installment(entity.getInstallment())
                        .issueCompanyCode(entity.getIssueCompanyCode())
                        .issueCompanyName(entity.getIssueCompanyName())
                        .purchaseCompanyCode(entity.getPurchaseCompanyCode())
                        .purchaseCompanyName(entity.getPurchaseCompanyName())
                        .bankCode(entity.getBankCode())
                        .bankName(entity.getBankName())
                        .billNumber(entity.getBillNumber())
                        .billSequenceNumber(entity.getBillSequenceNumber())
                        .userIp(entity.getUserIp())
                        .paymentInfo(entity.getPaymentInfo())
                        .paymentSource(entity.getPaymentSource())
                        .resultCode(entity.getResultCode())
                        .resultMessage(entity.getResultMessage())
                        .paidAt(entity.getPaidAt())
                        .createdAt(entity.getCreatedAt())
                        .modifiedAt(entity.getModifiedAt())
                        .build()
        );
    }
}
