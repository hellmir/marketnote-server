package com.personal.marketnote.commerce.adapter.out.persistence.ledger;

import com.personal.marketnote.commerce.adapter.out.persistence.ledger.mapper.AccountEntityToDomainMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.ledger.repository.AccountJpaRepository;
import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements FindAccountPort {
    private final AccountJpaRepository accountJpaRepository;

    @Override
    @Cacheable(
            value = "account:detail",
            key = "#id",
            unless = "#result == null || T(java.util.Optional).empty().equals(#result)"
    )
    public Optional<Account> findById(Long id) {
        return accountJpaRepository.findById(id)
                .map(AccountEntityToDomainMapper::toDomain);
    }

    @Override
    @Cacheable(
            value = "account:name",
            key = "#name",
            unless = "#result == null || T(java.util.Optional).empty().equals(#result)"
    )
    public Optional<Account> findByName(String name) {
        return accountJpaRepository.findByName(name)
                .map(AccountEntityToDomainMapper::toDomain);
    }
}
