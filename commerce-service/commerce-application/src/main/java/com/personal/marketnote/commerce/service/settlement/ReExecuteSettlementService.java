package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.exception.AccountNotFoundException;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.command.ledger.RecordLedgerEntryCommand;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.settlement.ReExecuteSettlementUseCase;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 취소된 정산을 재실행하는 서비스.
 * <p>
 * CANCELLED 상태의 정산을 PENDING으로 리셋한 후 분개를 재기록하고 COMPLETED로 전이한다.
 * PaymentAllocation은 취소 시에도 연결 상태를 유지하므로 재할당하지 않는다.
 * 분개 기록 중 실패하면 정산을 FAILED 상태로 저장한다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class ReExecuteSettlementService implements ReExecuteSettlementUseCase {
    private static final String ACCOUNT_PG_RECEIVABLE = "매출채권_PG";
    private static final String ACCOUNT_SELLER_PAYABLE = "미지급금_판매자";
    private static final String ACCOUNT_CASH = "보통예금";
    private static final String ACCOUNT_PG_FEE = "PG수수료비용";
    private static final String ACCOUNT_PLATFORM_FEE_REVENUE = "플랫폼수수료수익";

    private final FindSettlementPort findSettlementPort;
    private final UpdateSettlementPort updateSettlementPort;
    private final RecordLedgerEntryUseCase recordLedgerEntryUseCase;
    private final FindAccountPort findAccountPort;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void reExecuteSettlement(Long settlementId) {
        Settlement settlement = findSettlementPort.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException(settlementId));

        settlement.resetCancelledToPending();

        try {
            Long totalAllocatedAmount = settlement.getTotalAllocatedAmount();
            Long pgFeeAmount = settlement.getPgFeeAmount();
            Long platformFeeAmount = settlement.getPlatformFeeAmount();
            Long sellerPayoutAmount = settlement.getSellerPayoutAmount();

            recordPgSettlementReExecution(settlementId, totalAllocatedAmount, pgFeeAmount);

            long sellerSettlementDebit = sellerPayoutAmount + platformFeeAmount;
            recordSellerSettlementReExecution(settlementId, sellerSettlementDebit, sellerPayoutAmount, platformFeeAmount);

            settlement.complete();
        } catch (Exception e) {
            settlement.fail();
            updateSettlementPort.update(settlement);
            log.error("정산 재실행 실패 - settlementId: {}, error: {}", settlementId, e.getMessage(), e);
            throw e;
        }

        updateSettlementPort.update(settlement);

        log.info("정산 재실행 완료 - settlementId: {}, sellerId: {}, year: {}, month: {}",
                settlementId, settlement.getSellerId(), settlement.getYear(), settlement.getMonth());
    }

    /**
     * PG 정산 분개를 재기록한다.
     * <p>
     * 원래 PG 정산과 동일한 분개를 재실행용 idempotencyKey로 기록한다.
     * <pre>
     * DEBIT  보통예금         = totalAllocatedAmount - pgFeeAmount
     * DEBIT  PG수수료비용     = pgFeeAmount (if > 0)
     * CREDIT 매출채권_PG      = totalAllocatedAmount
     * </pre>
     * </p>
     */
    private void recordPgSettlementReExecution(Long settlementId, Long totalAllocatedAmount, Long pgFeeAmount) {
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_CASH));
        Account pgFeeAccount = findAccountPort.findByName(ACCOUNT_PG_FEE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_FEE));
        Account pgReceivable = findAccountPort.findByName(ACCOUNT_PG_RECEIVABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PG_RECEIVABLE));

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        long cashAmount = totalAllocatedAmount - pgFeeAmount;
        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(cashAccount.getId())
                .amount(cashAmount)
                .transactionType(TransactionType.DEBIT)
                .build());

        if (pgFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                    .accountId(pgFeeAccount.getId())
                    .amount(pgFeeAmount)
                    .transactionType(TransactionType.DEBIT)
                    .build());
        }

        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(pgReceivable.getId())
                .amount(totalAllocatedAmount)
                .transactionType(TransactionType.CREDIT)
                .build());

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.PG_SETTLEMENT)
                .targetType("SETTLEMENT")
                .targetId(settlementId)
                .description("PG 정산 재실행 분개")
                .idempotencyKey("PG_SETTLEMENT_REEXEC:" + settlementId)
                .entries(entries)
                .build();

        recordLedgerEntryUseCase.record(command);
    }

    /**
     * 판매자 정산 분개를 재기록한다.
     * <p>
     * 원래 판매자 정산과 동일한 분개를 재실행용 idempotencyKey로 기록한다.
     * <pre>
     * DEBIT  미지급금_판매자    = sellerPayoutAmount + platformFeeAmount
     * CREDIT 보통예금          = sellerPayoutAmount
     * CREDIT 플랫폼수수료수익   = platformFeeAmount (if > 0)
     * </pre>
     * </p>
     */
    private void recordSellerSettlementReExecution(Long settlementId, Long totalAmount, Long sellerPayoutAmount, Long platformFeeAmount) {
        Account sellerPayable = findAccountPort.findByName(ACCOUNT_SELLER_PAYABLE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_SELLER_PAYABLE));
        Account cashAccount = findAccountPort.findByName(ACCOUNT_CASH)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_CASH));
        Account platformFeeAccount = findAccountPort.findByName(ACCOUNT_PLATFORM_FEE_REVENUE)
                .orElseThrow(() -> new AccountNotFoundException(ACCOUNT_PLATFORM_FEE_REVENUE));

        List<RecordLedgerEntryCommand.EntryLine> entries = new ArrayList<>();

        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(sellerPayable.getId())
                .amount(totalAmount)
                .transactionType(TransactionType.DEBIT)
                .build());

        entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                .accountId(cashAccount.getId())
                .amount(sellerPayoutAmount)
                .transactionType(TransactionType.CREDIT)
                .build());

        if (platformFeeAmount > 0) {
            entries.add(RecordLedgerEntryCommand.EntryLine.builder()
                    .accountId(platformFeeAccount.getId())
                    .amount(platformFeeAmount)
                    .transactionType(TransactionType.CREDIT)
                    .build());
        }

        RecordLedgerEntryCommand command = RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.SELLER_SETTLEMENT)
                .targetType("SETTLEMENT")
                .targetId(settlementId)
                .description("판매자 정산 재실행 분개")
                .idempotencyKey("SELLER_SETTLEMENT_REEXEC:" + settlementId)
                .entries(entries)
                .build();

        recordLedgerEntryUseCase.record(command);
    }
}
