package com.personal.marketnote.commerce.port.in.command.ledger;

import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import lombok.Builder;

import java.util.List;

@Builder
public record RecordLedgerEntryCommand(
        LedgerTransactionType transactionType,
        String targetType,
        Long targetId,
        String description,
        String idempotencyKey,
        List<EntryLine> entries
) {
    private static final String TARGET_PAYMENT = "PAYMENT";
    private static final String TARGET_SETTLEMENT = "SETTLEMENT";

    @Builder
    public record EntryLine(
            Long accountId,
            Long amount,
            TransactionType transactionType
    ) {
        public static EntryLine debit(Long accountId, long amount) {
            return new EntryLine(accountId, amount, TransactionType.DEBIT);
        }

        public static EntryLine credit(Long accountId, long amount) {
            return new EntryLine(accountId, amount, TransactionType.CREDIT);
        }
    }

    public static RecordLedgerEntryCommand forPaymentApproval(Long orderId, List<EntryLine> entries) {
        return RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.PAYMENT_APPROVAL)
                .targetType(TARGET_PAYMENT)
                .targetId(orderId)
                .description("결제 승인 분개")
                .idempotencyKey("PAYMENT_APPROVAL:" + orderId)
                .entries(entries)
                .build();
    }

    public static RecordLedgerEntryCommand forPaymentCancellation(
            Long orderId, String idempotencyKey, List<EntryLine> entries
    ) {
        return RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.PAYMENT_CANCELLATION)
                .targetType(TARGET_PAYMENT)
                .targetId(orderId)
                .description("결제 취소/환불 역분개")
                .idempotencyKey(idempotencyKey)
                .entries(entries)
                .build();
    }

    public static RecordLedgerEntryCommand forPgSettlement(Long settlementId, List<EntryLine> entries) {
        return RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.PG_SETTLEMENT)
                .targetType(TARGET_SETTLEMENT)
                .targetId(settlementId)
                .description("PG 정산 입금 분개")
                .idempotencyKey("PG_SETTLEMENT:" + settlementId)
                .entries(entries)
                .build();
    }

    public static RecordLedgerEntryCommand forSellerSettlement(Long settlementId, List<EntryLine> entries) {
        return RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.SELLER_SETTLEMENT)
                .targetType(TARGET_SETTLEMENT)
                .targetId(settlementId)
                .description("판매자 정산 분개")
                .idempotencyKey("SELLER_SETTLEMENT:" + settlementId)
                .entries(entries)
                .build();
    }

    public static RecordLedgerEntryCommand forSettlementCancellation(Long settlementId, List<EntryLine> entries) {
        return RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.SETTLEMENT_CANCELLATION)
                .targetType(TARGET_SETTLEMENT)
                .targetId(settlementId)
                .description("정산 취소 역분개")
                .idempotencyKey("SETTLEMENT_CANCEL:" + settlementId)
                .entries(entries)
                .build();
    }

    public static RecordLedgerEntryCommand forPgSettlementReExec(Long settlementId, List<EntryLine> entries) {
        return RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.PG_SETTLEMENT)
                .targetType(TARGET_SETTLEMENT)
                .targetId(settlementId)
                .description("PG 정산 재실행 분개")
                .idempotencyKey("PG_SETTLEMENT_REEXEC:" + settlementId)
                .entries(entries)
                .build();
    }

    public static RecordLedgerEntryCommand forSellerSettlementReExec(Long settlementId, List<EntryLine> entries) {
        return RecordLedgerEntryCommand.builder()
                .transactionType(LedgerTransactionType.SELLER_SETTLEMENT)
                .targetType(TARGET_SETTLEMENT)
                .targetId(settlementId)
                .description("판매자 정산 재실행 분개")
                .idempotencyKey("SELLER_SETTLEMENT_REEXEC:" + settlementId)
                .entries(entries)
                .build();
    }
}
