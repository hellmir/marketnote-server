package com.personal.marketnote.commerce.port.in.usecase.ledger;

import com.personal.marketnote.commerce.port.in.result.ledger.GetAccountBalanceResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계정과목별 잔액 조회 UseCase
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface GetAccountBalanceUseCase {
    List<GetAccountBalanceResult> getAccountBalances(LocalDateTime asOf);
}
