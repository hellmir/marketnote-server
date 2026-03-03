package com.personal.marketnote.commerce.service.ledger;

import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.domain.ledger.AccountSnapshotState;
import com.personal.marketnote.commerce.domain.ledger.AccountType;
import com.personal.marketnote.commerce.exception.AccountNotFoundException;
import com.personal.marketnote.commerce.port.in.result.ledger.GetAccountBalanceResult;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.commerce.port.out.ledger.FindLedgerEntryPort;
import com.personal.marketnote.commerce.port.out.ledger.dto.AccountBalanceDto;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAccountBalanceUseCase 테스트")
class GetAccountBalanceUseCaseTest {

    @InjectMocks
    private GetAccountBalanceService getAccountBalanceService;

    @Mock
    private FindLedgerEntryPort findLedgerEntryPort;

    @Mock
    private FindAccountPort findAccountPort;

    private Account createAccount(Long id, String name, AccountType accountType) {
        return Account.from(AccountSnapshotState.builder()
                .id(id)
                .name(name)
                .accountType(accountType)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }

    @Test
    @DisplayName("자산 계정의 잔액은 DEBIT 합계 - CREDIT 합계로 계산된다")
    void shouldCalculateAssetBalanceAsDebitMinusCredit() {
        // given
        LocalDateTime asOf = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        AccountBalanceDto dto = new AccountBalanceDto(1L, 100000L, 50000L);
        when(findLedgerEntryPort.findAccountBalanceSummary(asOf)).thenReturn(List.of(dto));

        Account account = createAccount(1L, "매출채권_PG", AccountType.ASSET);
        when(findAccountPort.findById(1L)).thenReturn(Optional.of(account));

        // when
        List<GetAccountBalanceResult> results = getAccountBalanceService.getAccountBalances(asOf);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).accountName()).isEqualTo("매출채권_PG");
        assertThat(results.get(0).balance()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("부채 계정의 잔액은 CREDIT 합계 - DEBIT 합계로 계산된다")
    void shouldCalculateLiabilityBalanceAsCreditMinusDebit() {
        // given
        LocalDateTime asOf = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        AccountBalanceDto dto = new AccountBalanceDto(2L, 30000L, 80000L);
        when(findLedgerEntryPort.findAccountBalanceSummary(asOf)).thenReturn(List.of(dto));

        Account account = createAccount(2L, "미지급금_판매자", AccountType.LIABILITY);
        when(findAccountPort.findById(2L)).thenReturn(Optional.of(account));

        // when
        List<GetAccountBalanceResult> results = getAccountBalanceService.getAccountBalances(asOf);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).accountName()).isEqualTo("미지급금_판매자");
        assertThat(results.get(0).balance()).isEqualTo(50000L);
    }

    @Test
    @DisplayName("수익 계정의 잔액은 CREDIT 합계 - DEBIT 합계로 계산된다")
    void shouldCalculateRevenueBalanceAsCreditMinusDebit() {
        // given
        LocalDateTime asOf = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        AccountBalanceDto dto = new AccountBalanceDto(3L, 0L, 15000L);
        when(findLedgerEntryPort.findAccountBalanceSummary(asOf)).thenReturn(List.of(dto));

        Account account = createAccount(3L, "플랫폼수수료수익", AccountType.REVENUE);
        when(findAccountPort.findById(3L)).thenReturn(Optional.of(account));

        // when
        List<GetAccountBalanceResult> results = getAccountBalanceService.getAccountBalances(asOf);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).balance()).isEqualTo(15000L);
    }

    @Test
    @DisplayName("비용 계정의 잔액은 DEBIT 합계 - CREDIT 합계로 계산된다")
    void shouldCalculateExpenseBalanceAsDebitMinusCredit() {
        // given
        LocalDateTime asOf = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        AccountBalanceDto dto = new AccountBalanceDto(4L, 5000L, 0L);
        when(findLedgerEntryPort.findAccountBalanceSummary(asOf)).thenReturn(List.of(dto));

        Account account = createAccount(4L, "PG수수료비용", AccountType.EXPENSE);
        when(findAccountPort.findById(4L)).thenReturn(Optional.of(account));

        // when
        List<GetAccountBalanceResult> results = getAccountBalanceService.getAccountBalances(asOf);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).balance()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("여러 계정의 잔액을 한 번에 조회한다")
    void shouldGetMultipleAccountBalances() {
        // given
        LocalDateTime asOf = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        AccountBalanceDto dto1 = new AccountBalanceDto(1L, 100000L, 50000L);
        AccountBalanceDto dto2 = new AccountBalanceDto(2L, 30000L, 80000L);
        when(findLedgerEntryPort.findAccountBalanceSummary(asOf)).thenReturn(List.of(dto1, dto2));

        Account account1 = createAccount(1L, "매출채권_PG", AccountType.ASSET);
        Account account2 = createAccount(2L, "미지급금_판매자", AccountType.LIABILITY);
        when(findAccountPort.findById(1L)).thenReturn(Optional.of(account1));
        when(findAccountPort.findById(2L)).thenReturn(Optional.of(account2));

        // when
        List<GetAccountBalanceResult> results = getAccountBalanceService.getAccountBalances(asOf);

        // then
        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("분개 내역이 없으면 빈 리스트를 반환한다")
    void shouldReturnEmptyListWhenNoEntries() {
        // given
        LocalDateTime asOf = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        when(findLedgerEntryPort.findAccountBalanceSummary(asOf)).thenReturn(List.of());

        // when
        List<GetAccountBalanceResult> results = getAccountBalanceService.getAccountBalances(asOf);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 계정이면 예외를 던진다")
    void shouldThrowWhenAccountNotFound() {
        // given
        LocalDateTime asOf = LocalDateTime.of(2026, 2, 28, 23, 59, 59);
        AccountBalanceDto dto = new AccountBalanceDto(999L, 10000L, 5000L);
        when(findLedgerEntryPort.findAccountBalanceSummary(asOf)).thenReturn(List.of(dto));
        when(findAccountPort.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getAccountBalanceService.getAccountBalances(asOf))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
