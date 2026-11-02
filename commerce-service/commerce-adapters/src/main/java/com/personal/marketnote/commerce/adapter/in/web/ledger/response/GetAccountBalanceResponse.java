package com.personal.marketnote.commerce.adapter.in.web.ledger.response;

import com.personal.marketnote.commerce.domain.ledger.AccountType;
import com.personal.marketnote.commerce.port.in.result.ledger.GetAccountBalanceResult;

public record GetAccountBalanceResponse(
        Long accountId,
        String accountName,
        AccountType accountType,
        String accountTypeDescription,
        Long debitTotal,
        Long creditTotal,
        Long balance
) {
    public static GetAccountBalanceResponse from(GetAccountBalanceResult result) {
        return new GetAccountBalanceResponse(
                result.accountId(),
                result.accountName(),
                result.accountType(),
                result.accountType().getDescription(),
                result.debitTotal(),
                result.creditTotal(),
                result.balance()
        );
    }
}
