package com.personal.marketnote.commerce.port.out.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerEntry;

import java.util.List;

public interface SaveLedgerEntryPort {
    void saveAll(List<LedgerEntry> entries);
}
