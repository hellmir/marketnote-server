package com.personal.marketnote.commerce.adapter.in.web.ledger.response;

import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.port.in.result.ledger.GetLedgerTransactionResult;

import java.time.LocalDateTime;
import java.util.List;

public record GetLedgerTransactionResponse(
        Long id,
        LedgerTransactionType transactionType,
        String transactionTypeDescription,
        String targetType,
        Long targetId,
        String description,
        String idempotencyKey,
        LocalDateTime createdAt,
        List<GetLedgerEntryResponse> entries
) {
    public static GetLedgerTransactionResponse from(GetLedgerTransactionResult result) {
        List<GetLedgerEntryResponse> entryResponses = result.entries().stream()
                .map(GetLedgerEntryResponse::from)
                .toList();

        return new GetLedgerTransactionResponse(
                result.id(),
                result.transactionType(),
                result.transactionType().getDescription(),
                result.targetType(),
                result.targetId(),
                result.description(),
                result.idempotencyKey(),
                result.createdAt(),
                entryResponses
        );
    }
}
