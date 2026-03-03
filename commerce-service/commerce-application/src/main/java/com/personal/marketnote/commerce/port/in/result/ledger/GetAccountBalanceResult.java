package com.personal.marketnote.commerce.port.in.result.ledger;

import com.personal.marketnote.commerce.domain.ledger.AccountBalance;
import com.personal.marketnote.commerce.domain.ledger.AccountType;
import lombok.Builder;

@Builder
public record GetAccountBalanceResult(
        Long accountId,
        String accountName,
        AccountType accountType,
        Long debitTotal,
        Long creditTotal,
        Long balance
) {
    public static GetAccountBalanceResult from(AccountBalance accountBalance) {
        return GetAccountBalanceResult.builder()
                .accountId(accountBalance.getAccountId())
                .accountName(accountBalance.getAccountName())
                .accountType(accountBalance.getAccountType())
                .debitTotal(accountBalance.getDebitTotal())
                .creditTotal(accountBalance.getCreditTotal())
                .balance(accountBalance.getBalance())
                .build();
    }
}
