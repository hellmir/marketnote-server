package com.personal.marketnote.commerce.adapter.in.web.ledger.response;

import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import com.personal.marketnote.commerce.port.in.result.ledger.GetLedgerEntryResult;

import java.time.LocalDateTime;

public record GetLedgerEntryResponse(
        Long id,
        Long accountId,
        Long transactionId,
        Long amount,
        TransactionType transactionType,
        String transactionTypeDescription,
        LocalDateTime createdAt
) {
    public static GetLedgerEntryResponse from(GetLedgerEntryResult result) {
        return new GetLedgerEntryResponse(
                result.id(),
                result.accountId(),
                result.transactionId(),
                result.amount(),
                result.transactionType(),
                result.transactionType().getDescription(),
                result.createdAt()
        );
    }
}
