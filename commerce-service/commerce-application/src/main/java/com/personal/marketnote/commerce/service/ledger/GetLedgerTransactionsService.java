package com.personal.marketnote.commerce.service.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerEntry;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;
import com.personal.marketnote.commerce.port.in.command.ledger.GetLedgerTransactionsQuery;
import com.personal.marketnote.commerce.port.in.result.ledger.GetLedgerEntryResult;
import com.personal.marketnote.commerce.port.in.result.ledger.GetLedgerTransactionResult;
import com.personal.marketnote.commerce.port.in.usecase.ledger.GetLedgerTransactionsUseCase;
import com.personal.marketnote.commerce.port.out.ledger.FindLedgerEntryPort;
import com.personal.marketnote.commerce.port.out.ledger.FindLedgerTransactionPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 회계 거래 목록 조회 서비스
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetLedgerTransactionsService implements GetLedgerTransactionsUseCase {
    private final FindLedgerTransactionPort findLedgerTransactionPort;
    private final FindLedgerEntryPort findLedgerEntryPort;

    @Override
    public List<GetLedgerTransactionResult> getLedgerTransactions(GetLedgerTransactionsQuery query) {
        List<LedgerTransaction> transactions = findLedgerTransactionPort.findByFilters(
                query.startDate(),
                query.endDate(),
                query.transactionType()
        );

        return transactions.stream()
                .map(this::toResultWithEntries)
                .toList();
    }

    private GetLedgerTransactionResult toResultWithEntries(LedgerTransaction transaction) {
        List<LedgerEntry> entries = findLedgerEntryPort.findByTransactionId(transaction.getId());
        List<GetLedgerEntryResult> entryResults = entries.stream()
                .map(GetLedgerEntryResult::from)
                .toList();
        return GetLedgerTransactionResult.from(transaction, entryResults);
    }
}
