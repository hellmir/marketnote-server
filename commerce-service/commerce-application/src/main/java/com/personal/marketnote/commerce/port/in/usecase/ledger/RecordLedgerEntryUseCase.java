package com.personal.marketnote.commerce.port.in.usecase.ledger;

import com.personal.marketnote.commerce.port.in.command.ledger.RecordLedgerEntryCommand;

public interface RecordLedgerEntryUseCase {
    void record(RecordLedgerEntryCommand command);

    void recordPaymentApproval(Long orderId, long paymentAmount);

    void recordPaymentCancellation(Long orderId, long cancelAmount, String idempotencyKey);

    void recordPgSettlement(Long settlementId, long totalAmount, long pgFeeAmount);

    void recordSellerSettlement(Long settlementId, long totalAmount, long sellerPayoutAmount, long platformFeeAmount);
}
