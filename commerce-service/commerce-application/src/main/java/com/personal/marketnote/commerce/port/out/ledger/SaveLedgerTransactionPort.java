package com.personal.marketnote.commerce.port.out.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;

public interface SaveLedgerTransactionPort {
    LedgerTransaction save(LedgerTransaction transaction);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
