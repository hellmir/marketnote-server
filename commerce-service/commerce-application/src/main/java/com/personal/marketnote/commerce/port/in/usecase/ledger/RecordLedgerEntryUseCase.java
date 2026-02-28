package com.personal.marketnote.commerce.port.in.usecase.ledger;

import com.personal.marketnote.commerce.port.in.command.ledger.RecordLedgerEntryCommand;

public interface RecordLedgerEntryUseCase {
    void record(RecordLedgerEntryCommand command);

    void recordPaymentApproval(Long orderId, long paymentAmount);
}
