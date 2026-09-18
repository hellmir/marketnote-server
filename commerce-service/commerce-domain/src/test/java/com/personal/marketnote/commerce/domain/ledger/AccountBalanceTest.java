package com.personal.marketnote.commerce.domain.ledger;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountBalance 도메인 테스트")
class AccountBalanceTest {

    private Account createAccount(AccountType accountType) {
        return Account.from(AccountSnapshotState.builder()
                .id(1L)
                .name("테스트 계정")
                .accountType(accountType)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .modifiedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build());
    }

    @Nested
    @DisplayName("ASSET/EXPENSE 계정 잔액 계산")
    class AssetExpenseBalance {

        @Test
        @DisplayName("ASSET 계정의 잔액은 차변 합계에서 대변 합계를 뺀 값이다")
        void shouldCalculateAssetBalanceAsDebitMinusCredit() {
            // given
            Account account = createAccount(AccountType.ASSET);

            // when
            AccountBalance balance = AccountBalance.of(account, 100000L, 30000L);

            // then
            assertThat(balance.getBalance()).isEqualTo(70000L);
        }

        @Test
        @DisplayName("EXPENSE 계정의 잔액은 차변 합계에서 대변 합계를 뺀 값이다")
        void shouldCalculateExpenseBalanceAsDebitMinusCredit() {
            // given
            Account account = createAccount(AccountType.EXPENSE);

            // when
            AccountBalance balance = AccountBalance.of(account, 50000L, 20000L);

            // then
            assertThat(balance.getBalance()).isEqualTo(30000L);
        }
    }

    @Nested
    @DisplayName("LIABILITY/EQUITY/REVENUE 계정 잔액 계산")
    class LiabilityEquityRevenueBalance {

        @Test
        @DisplayName("LIABILITY 계정의 잔액은 대변 합계에서 차변 합계를 뺀 값이다")
        void shouldCalculateLiabilityBalanceAsCreditMinusDebit() {
            // given
            Account account = createAccount(AccountType.LIABILITY);

            // when
            AccountBalance balance = AccountBalance.of(account, 30000L, 100000L);

            // then
            assertThat(balance.getBalance()).isEqualTo(70000L);
        }

        @Test
        @DisplayName("EQUITY 계정의 잔액은 대변 합계에서 차변 합계를 뺀 값이다")
        void shouldCalculateEquityBalanceAsCreditMinusDebit() {
            // given
            Account account = createAccount(AccountType.EQUITY);

            // when
            AccountBalance balance = AccountBalance.of(account, 10000L, 60000L);

            // then
            assertThat(balance.getBalance()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("REVENUE 계정의 잔액은 대변 합계에서 차변 합계를 뺀 값이다")
        void shouldCalculateRevenueBalanceAsCreditMinusDebit() {
            // given
            Account account = createAccount(AccountType.REVENUE);

            // when
            AccountBalance balance = AccountBalance.of(account, 5000L, 80000L);

            // then
            assertThat(balance.getBalance()).isEqualTo(75000L);
        }
    }

    @Nested
    @DisplayName("계정 정보 매핑")
    class AccountInfoMapping {

        @Test
        @DisplayName("계정 정보가 올바르게 매핑된다")
        void shouldMapAccountInfoCorrectly() {
            // given
            Account account = Account.from(AccountSnapshotState.builder()
                    .id(42L)
                    .name("매출채권")
                    .accountType(AccountType.ASSET)
                    .status(EntityStatus.ACTIVE)
                    .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                    .modifiedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                    .build());

            // when
            AccountBalance balance = AccountBalance.of(account, 200000L, 50000L);

            // then
            assertThat(balance.getAccountId()).isEqualTo(42L);
            assertThat(balance.getAccountName()).isEqualTo("매출채권");
            assertThat(balance.getAccountType()).isEqualTo(AccountType.ASSET);
            assertThat(balance.getDebitTotal()).isEqualTo(200000L);
            assertThat(balance.getCreditTotal()).isEqualTo(50000L);
            assertThat(balance.getBalance()).isEqualTo(150000L);
        }
    }
}
