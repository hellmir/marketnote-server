package com.personal.marketnote.commerce.port.out.ledger.dto;

public record AccountBalanceDto(
        Long accountId,
        Long debitTotal,
        Long creditTotal
) {
}
