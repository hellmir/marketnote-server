package com.personal.marketnote.commerce.port.out.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;

import java.time.LocalDateTime;
import java.util.List;

public interface FindLedgerTransactionPort {
    List<LedgerTransaction> findByFilters(LocalDateTime startDate, LocalDateTime endDate, LedgerTransactionType transactionType);
}
