package com.personal.marketnote.commerce.port.in.command.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GetLedgerTransactionsQuery(
        LocalDateTime startDate,
        LocalDateTime endDate,
        LedgerTransactionType transactionType
) {
}
