package com.personal.marketnote.commerce.port.out.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerEntry;
import com.personal.marketnote.commerce.port.out.ledger.dto.AccountBalanceDto;

import java.time.LocalDateTime;
import java.util.List;

public interface FindLedgerEntryPort {
    List<LedgerEntry> findByTransactionId(Long transactionId);

    List<AccountBalanceDto> findAccountBalanceSummary(LocalDateTime asOf);
}
