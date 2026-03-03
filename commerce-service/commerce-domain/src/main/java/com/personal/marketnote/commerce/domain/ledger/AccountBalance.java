package com.personal.marketnote.commerce.domain.ledger;

import lombok.*;

/**
 * 계정별 잔액 도메인 모델
 *
 * <p>계정별 DEBIT/CREDIT 누적 합계와 잔액을 계산한다.
 * 잔액 계산 규칙:
 * - 자산(ASSET), 비용(EXPENSE): DEBIT 합계 - CREDIT 합계
 * - 부채(LIABILITY), 자본(EQUITY), 수익(REVENUE): CREDIT 합계 - DEBIT 합계
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class AccountBalance {
    private Long accountId;
    private String accountName;
    private AccountType accountType;
    private Long debitTotal;
    private Long creditTotal;
    private Long balance;

    public static AccountBalance of(Account account, Long debitTotal, Long creditTotal) {
        Long calculatedBalance = calculateBalance(account.getAccountType(), debitTotal, creditTotal);

        return AccountBalance.builder()
                .accountId(account.getId())
                .accountName(account.getName())
                .accountType(account.getAccountType())
                .debitTotal(debitTotal)
                .creditTotal(creditTotal)
                .balance(calculatedBalance)
                .build();
    }

    private static Long calculateBalance(AccountType accountType, Long debitTotal, Long creditTotal) {
        if (accountType.isAsset() || accountType.isExpense()) {
            return Math.subtractExact(debitTotal, creditTotal);
        }
        return Math.subtractExact(creditTotal, debitTotal);
    }
}
