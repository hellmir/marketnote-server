package com.personal.marketnote.commerce.port.in.usecase.ledger;

import com.personal.marketnote.commerce.port.in.command.ledger.GetLedgerTransactionsQuery;
import com.personal.marketnote.commerce.port.in.result.ledger.GetLedgerTransactionResult;

import java.util.List;

/**
 * 회계 거래 목록 조회 UseCase
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface GetLedgerTransactionsUseCase {
    List<GetLedgerTransactionResult> getLedgerTransactions(GetLedgerTransactionsQuery query);
}
