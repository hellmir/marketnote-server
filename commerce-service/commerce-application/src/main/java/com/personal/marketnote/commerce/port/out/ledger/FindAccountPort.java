package com.personal.marketnote.commerce.port.out.ledger;

import com.personal.marketnote.commerce.domain.ledger.Account;

import java.util.Optional;

public interface FindAccountPort {
    Optional<Account> findById(Long id);

    Optional<Account> findByName(String name);
}
