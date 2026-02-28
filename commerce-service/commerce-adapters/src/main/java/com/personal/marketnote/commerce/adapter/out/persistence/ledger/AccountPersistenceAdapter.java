package com.personal.marketnote.commerce.adapter.out.persistence.ledger;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.mapper.AccountEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.AccountJpaRepository;
import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements FindAccountPort {
    private final AccountJpaRepository accountJpaRepository;

    @Override
    public Optional<Account> findById(Long id) {
        return accountJpaRepository.findById(id)
                .map(AccountEntityToDomainMapper::toDomain);
    }

    @Override
    public Optional<Account> findByName(String name) {
        return accountJpaRepository.findByName(name)
                .map(AccountEntityToDomainMapper::toDomain);
    }
}
