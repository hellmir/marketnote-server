package com.personal.marketnote.commerce.port.in.command.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import lombok.Builder;

import java.util.List;

@Builder
public record RecordLedgerEntryCommand(
        LedgerTransactionType transactionType,
        String targetType,
        Long targetId,
        String description,
        String idempotencyKey,
        List<EntryLine> entries
) {
    @Builder
    public record EntryLine(
            Long accountId,
            Long amount,
            TransactionType transactionType
    ) {
    }
}
