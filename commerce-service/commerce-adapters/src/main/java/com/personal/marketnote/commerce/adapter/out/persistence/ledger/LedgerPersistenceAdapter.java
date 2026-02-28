package com.personal.marketnote.commerce.adapter.out.persistence.ledger;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.LedgerEntryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.entity.LedgerTransactionJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.mapper.LedgerTransactionEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.LedgerEntryJpaRepository;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.LedgerTransactionJpaRepository;
import com.personal.marketnote.commerce.domain.ledger.LedgerEntry;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransaction;
import com.personal.marketnote.commerce.exception.DuplicateLedgerTransactionException;
import com.personal.marketnote.commerce.port.out.ledger.SaveLedgerEntryPort;
import com.personal.marketnote.commerce.port.out.ledger.SaveLedgerTransactionPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class LedgerPersistenceAdapter implements SaveLedgerTransactionPort, SaveLedgerEntryPort {
    private final LedgerTransactionJpaRepository ledgerTransactionJpaRepository;
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Override
    public LedgerTransaction save(LedgerTransaction transaction) {
        LedgerTransactionJpaEntity entity = LedgerTransactionJpaEntity.from(transaction);
        try {
            LedgerTransactionJpaEntity savedEntity = ledgerTransactionJpaRepository.saveAndFlush(entity);
            return LedgerTransactionEntityToDomainMapper.toDomain(savedEntity);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateLedgerTransactionException(transaction.getIdempotencyKey());
        }
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return ledgerTransactionJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public void saveAll(List<LedgerEntry> entries) {
        List<LedgerEntryJpaEntity> entities = entries.stream()
                .map(LedgerEntryJpaEntity::from)
                .toList();
        ledgerEntryJpaRepository.saveAll(entities);
    }
}
