package com.personal.marketnote.commerce.service.ledger;

import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.domain.ledger.AccountBalance;
import com.personal.marketnote.commerce.exception.AccountNotFoundException;
import com.personal.marketnote.commerce.port.in.result.ledger.GetAccountBalanceResult;
import com.personal.marketnote.commerce.port.in.usecase.ledger.GetAccountBalanceUseCase;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.commerce.port.out.ledger.FindLedgerEntryPort;
import com.personal.marketnote.commerce.port.out.ledger.dto.AccountBalanceDto;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 계정과목별 잔액 조회 서비스
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAccountBalanceService implements GetAccountBalanceUseCase {
    private final FindLedgerEntryPort findLedgerEntryPort;
    private final FindAccountPort findAccountPort;

    @Override
    public List<GetAccountBalanceResult> getAccountBalances(LocalDateTime asOf) {
        List<AccountBalanceDto> balanceDtos = findLedgerEntryPort.findAccountBalanceSummary(asOf);

        return balanceDtos.stream()
                .map(this::toAccountBalanceResult)
                .toList();
    }

    private GetAccountBalanceResult toAccountBalanceResult(AccountBalanceDto dto) {
        Account account = findAccountPort.findById(dto.accountId())
                .orElseThrow(() -> new AccountNotFoundException(dto.accountId()));

        AccountBalance accountBalance = AccountBalance.of(account, dto.debitTotal(), dto.creditTotal());
        return GetAccountBalanceResult.from(accountBalance);
    }
}
