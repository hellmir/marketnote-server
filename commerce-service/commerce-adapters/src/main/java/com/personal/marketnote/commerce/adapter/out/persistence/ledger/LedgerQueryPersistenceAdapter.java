package com.personal.marketnote.commerce.adapter.out.persistence.ledger;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.mapper.LedgerEntryEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.mapper.LedgerTransactionEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.LedgerEntryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.LedgerTransactionJpaRepository;
import com.personal.marketnote.commerce.domain.ledger.LedgerEntry;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import com.personal.marketnote.commerce.port.out.ledger.FindLedgerEntryPort;
import com.personal.marketnote.commerce.port.out.ledger.FindLedgerTransactionPort;
import com.personal.marketnote.commerce.port.out.ledger.dto.AccountBalanceDto;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 회계 장부 조회 퍼시스턴스 어댑터
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@PersistenceAdapter
@RequiredArgsConstructor
public class LedgerQueryPersistenceAdapter implements FindLedgerTransactionPort, FindLedgerEntryPort {
    private final LedgerTransactionJpaRepository ledgerTransactionJpaRepository;
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Override
    public List<LedgerTransaction> findByFilters(
            LocalDateTime startDate,
            LocalDateTime endDate,
            LedgerTransactionType transactionType
    ) {
        return ledgerTransactionJpaRepository.findByFilters(startDate, endDate, transactionType)
                .stream()
                .map(LedgerTransactionEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public List<LedgerEntry> findByTransactionId(Long transactionId) {
        return ledgerEntryJpaRepository.findByTransactionIdOrderByIdAsc(transactionId)
                .stream()
                .map(LedgerEntryEntityToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public List<AccountBalanceDto> findAccountBalanceSummary(LocalDateTime asOf) {
        List<Object[]> rows = ledgerEntryJpaRepository.findAccountBalanceSummary(asOf);

        Map<Long, Long> debitMap = new HashMap<>();
        Map<Long, Long> creditMap = new HashMap<>();

        for (Object[] row : rows) {
            Long accountId = (Long) row[0];
            TransactionType transactionType = (TransactionType) row[1];
            Long totalAmount = (Long) row[2];

            if (transactionType.isDebit()) {
                debitMap.put(accountId, totalAmount);
            } else {
                creditMap.put(accountId, totalAmount);
            }
        }

        List<Long> accountIds = new ArrayList<>(debitMap.keySet());
        for (Long accountId : creditMap.keySet()) {
            if (!debitMap.containsKey(accountId)) {
                accountIds.add(accountId);
            }
        }
        accountIds.sort(Long::compareTo);

        return accountIds.stream()
                .map(accountId -> new AccountBalanceDto(
                        accountId,
                        debitMap.getOrDefault(accountId, 0L),
                        creditMap.getOrDefault(accountId, 0L)
                ))
                .toList();
    }
}
