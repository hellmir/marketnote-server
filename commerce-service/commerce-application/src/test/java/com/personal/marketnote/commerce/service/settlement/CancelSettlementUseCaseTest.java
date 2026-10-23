package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.ledger.Account;
import com.personal.marketnote.commerce.domain.ledger.AccountSnapshotState;
import com.personal.marketnote.commerce.domain.ledger.AccountType;
import com.personal.marketnote.commerce.domain.ledger.LedgerTransactionType;
import com.personal.marketnote.commerce.domain.ledger.TransactionType;
import com.personal.marketnote.commerce.domain.settlement.InvalidSettlementStatusTransitionException;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import com.personal.marketnote.commerce.exception.AccountNotFoundException;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.command.ledger.RecordLedgerEntryCommand;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPort;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelSettlementUseCase 테스트")
class CancelSettlementUseCaseTest {

    @InjectMocks
    private CancelSettlementService cancelSettlementService;

    @Mock
    private FindSettlementPort findSettlementPort;

    @Mock
    private UpdateSettlementPort updateSettlementPort;

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Mock
    private FindAccountPort findAccountPort;

    private static final Long PG_RECEIVABLE_ID = 1L;
    private static final Long SELLER_PAYABLE_ID = 2L;
    private static final Long CASH_ID = 3L;
    private static final Long PG_FEE_ID = 4L;
    private static final Long PLATFORM_FEE_ID = 5L;

    @BeforeEach
    void setUpAccounts() {
        lenient().when(findAccountPort.findByName("매출채권_PG"))
                .thenReturn(Optional.of(createAccount(PG_RECEIVABLE_ID, "매출채권_PG")));
        lenient().when(findAccountPort.findByName("미지급금_판매자"))
                .thenReturn(Optional.of(createAccount(SELLER_PAYABLE_ID, "미지급금_판매자")));
        lenient().when(findAccountPort.findByName("보통예금")).thenReturn(Optional.of(createAccount(CASH_ID, "보통예금")));
        lenient().when(findAccountPort.findByName("PG수수료비용"))
                .thenReturn(Optional.of(createAccount(PG_FEE_ID, "PG수수료비용")));
        lenient().when(findAccountPort.findByName("플랫폼수수료수익"))
                .thenReturn(Optional.of(createAccount(PLATFORM_FEE_ID, "플랫폼수수료수익")));
    }

    private Account createAccount(Long id, String name) {
        return Account.from(AccountSnapshotState.builder()
                .id(id)
                .name(name)
                .accountType(AccountType.ASSET)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Settlement createCompletedSettlement(
            Long id, Long sellerId, Long totalAllocatedAmount,
            Long pgFeeAmount, Long platformFeeAmount, Long sellerPayoutAmount
    ) {
        return Settlement.from(SettlementSnapshotState.builder()
                .id(id)
                .sellerId(sellerId)
                .year(2026)
                .month(2)
                .totalAllocatedAmount(totalAllocatedAmount)
                .pgFeeAmount(pgFeeAmount)
                .platformFeeAmount(platformFeeAmount)
                .sellerPayoutAmount(sellerPayoutAmount)
                .status(SettlementStatus.COMPLETED)
                .version(1L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .shippingFee(0L)
                .build());
    }

    private long sumByType(List<RecordLedgerEntryCommand.EntryLine> entries, TransactionType type) {
        return entries.stream()
                .filter(entry -> entry.transactionType() == type)
                .mapToLong(RecordLedgerEntryCommand.EntryLine::amount)
                .sum();
    }

    private RecordLedgerEntryCommand.EntryLine findEntry(
            List<RecordLedgerEntryCommand.EntryLine> entries, Long accountId, TransactionType type
    ) {
        return entries.stream()
                .filter(entry -> entry.accountId().equals(accountId) && entry.transactionType() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError(
                        "Entry not found: accountId=" + accountId + ", type=" + type
                ));
    }

    @Nested
    @DisplayName("취소 성공")
    class CancelSuccess {

        @Test
        @DisplayName("완료된 정산을 취소하면 단일 LedgerTransaction에 6개 entries를 묶어 기록하고 CANCELLED 상태로 전이한다")
        void shouldCancelCompletedSettlementWithSingleTransaction() {
            // given
            Settlement completedSettlement = createCompletedSettlement(1L, 10L, 100000L, 3000L, 5000L, 92000L);
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(completedSettlement));

            // when
            cancelSettlementService.cancelSettlement(1L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase, times(1)).record(captor.capture());

            RecordLedgerEntryCommand command = captor.getValue();
            assertThat(command.entries()).hasSize(6);
            verify(updateSettlementPort).update(argThat(Settlement::isCancelled));
        }

        @Test
        @DisplayName("recordLedgerEntryUseCase.record() 는 정확히 1회만 호출된다")
        void shouldCallRecordExactlyOnce() {
            // given
            Settlement settlement = createCompletedSettlement(10L, 10L, 100000L, 3000L, 5000L, 92000L);
            when(findSettlementPort.findById(10L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(10L);

            // then
            verify(recordLedgerEntryUseCase, times(1)).record(any(RecordLedgerEntryCommand.class));
        }

        @Test
        @DisplayName("idempotencyKey는 SETTLEMENT_CANCEL:{id} 형식의 단일 값이다")
        void shouldUseUnifiedIdempotencyKey() {
            // given
            Settlement settlement = createCompletedSettlement(42L, 10L, 100000L, 3000L, 5000L, 92000L);
            when(findSettlementPort.findById(42L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(42L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase).record(captor.capture());

            RecordLedgerEntryCommand command = captor.getValue();
            assertThat(command.idempotencyKey()).isEqualTo("SETTLEMENT_CANCEL:42");
            assertThat(command.transactionType()).isEqualTo(LedgerTransactionType.SETTLEMENT_CANCELLATION);
            assertThat(command.targetType()).isEqualTo("SETTLEMENT");
            assertThat(command.targetId()).isEqualTo(42L);
            assertThat(command.description()).isEqualTo("정산 취소 역분개");
        }

        @Test
        @DisplayName("통합 분개의 DEBIT 합계는 CREDIT 합계와 일치한다")
        void shouldBalanceDebitAndCreditSums() {
            // given
            Settlement settlement = createCompletedSettlement(5L, 30L, 200000L, 6000L, 10000L, 184000L);
            when(findSettlementPort.findById(5L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(5L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase).record(captor.capture());

            RecordLedgerEntryCommand command = captor.getValue();
            long debitTotal = sumByType(command.entries(), TransactionType.DEBIT);
            long creditTotal = sumByType(command.entries(), TransactionType.CREDIT);
            assertThat(debitTotal).isEqualTo(creditTotal);
            // 기대 합계: totalAllocated(200000) + sellerPayout(184000) + platformFee(10000) = 394000
            assertThat(debitTotal).isEqualTo(394000L);
        }

        @Test
        @DisplayName("PG 수수료가 0이면 PG수수료비용 CREDIT 항목은 제외된다")
        void shouldExcludePgFeeEntryWhenZero() {
            // given
            Settlement settlement = createCompletedSettlement(2L, 20L, 50000L, 0L, 2500L, 47500L);
            when(findSettlementPort.findById(2L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(2L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase).record(captor.capture());

            RecordLedgerEntryCommand command = captor.getValue();
            assertThat(command.entries()).hasSize(5);
            assertThat(command.entries())
                    .noneMatch(entry -> entry.accountId().equals(PG_FEE_ID));

            long debitTotal = sumByType(command.entries(), TransactionType.DEBIT);
            long creditTotal = sumByType(command.entries(), TransactionType.CREDIT);
            assertThat(debitTotal).isEqualTo(creditTotal);
        }

        @Test
        @DisplayName("플랫폼 수수료가 0이면 플랫폼수수료수익 DEBIT 항목은 제외된다")
        void shouldExcludePlatformFeeEntryWhenZero() {
            // given
            Settlement settlement = createCompletedSettlement(3L, 30L, 80000L, 4000L, 0L, 76000L);
            when(findSettlementPort.findById(3L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(3L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase).record(captor.capture());

            RecordLedgerEntryCommand command = captor.getValue();
            assertThat(command.entries()).hasSize(5);
            assertThat(command.entries())
                    .noneMatch(entry -> entry.accountId().equals(PLATFORM_FEE_ID));

            long debitTotal = sumByType(command.entries(), TransactionType.DEBIT);
            long creditTotal = sumByType(command.entries(), TransactionType.CREDIT);
            assertThat(debitTotal).isEqualTo(creditTotal);
        }
    }

    @Nested
    @DisplayName("취소 실패")
    class CancelFailure {

        @Test
        @DisplayName("존재하지 않는 정산 ID로 취소하면 SettlementNotFoundException을 던진다")
        void shouldThrowWhenSettlementNotFound() {
            // given
            when(findSettlementPort.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cancelSettlementService.cancelSettlement(999L))
                    .isInstanceOf(SettlementNotFoundException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("계정 조회 실패 시 AccountNotFoundException을 던진다")
        void shouldThrowWhenAccountNotFound() {
            // given
            Settlement settlement = createCompletedSettlement(1L, 10L, 100000L, 3000L, 5000L, 92000L);
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(settlement));
            when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cancelSettlementService.cancelSettlement(1L))
                    .isInstanceOf(AccountNotFoundException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("PENDING 상태의 정산을 취소하면 InvalidSettlementStatusTransitionException을 던진다")
        void shouldThrowWhenSettlementIsPending() {
            // given
            Settlement pendingSettlement = Settlement.from(SettlementSnapshotState.builder()
                    .id(1L).sellerId(10L).year(2026).month(2)
                    .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                    .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                    .status(SettlementStatus.PENDING).version(0L)
                    .createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now())
                    .shippingFee(0L)
                    .build());
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(pendingSettlement));

            // when & then
            assertThatThrownBy(() -> cancelSettlementService.cancelSettlement(1L))
                    .isInstanceOf(InvalidSettlementStatusTransitionException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("FAILED 상태의 정산을 취소하면 InvalidSettlementStatusTransitionException을 던진다")
        void shouldThrowWhenSettlementIsFailed() {
            // given
            Settlement failedSettlement = Settlement.from(SettlementSnapshotState.builder()
                    .id(1L).sellerId(10L).year(2026).month(2)
                    .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                    .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                    .status(SettlementStatus.FAILED).version(0L)
                    .createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now())
                    .shippingFee(0L)
                    .build());
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(failedSettlement));

            // when & then
            assertThatThrownBy(() -> cancelSettlementService.cancelSettlement(1L))
                    .isInstanceOf(InvalidSettlementStatusTransitionException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("CANCELLED 상태의 정산을 취소하면 InvalidSettlementStatusTransitionException을 던진다")
        void shouldThrowWhenSettlementIsCancelled() {
            // given
            Settlement cancelledSettlement = Settlement.from(SettlementSnapshotState.builder()
                    .id(1L).sellerId(10L).year(2026).month(2)
                    .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                    .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                    .status(SettlementStatus.CANCELLED).version(2L)
                    .createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now())
                    .shippingFee(0L)
                    .build());
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(cancelledSettlement));

            // when & then
            assertThatThrownBy(() -> cancelSettlementService.cancelSettlement(1L))
                    .isInstanceOf(InvalidSettlementStatusTransitionException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }
    }

    @Nested
    @DisplayName("통합 역분개 검증")
    class UnifiedReverseLedgerVerification {

        @Test
        @DisplayName("통합 분개에는 PG 역분개 3개(DEBIT 매출채권_PG, CREDIT 보통예금, CREDIT PG수수료비용) 항목이 포함된다")
        void shouldIncludePgReversalEntries() {
            // given
            Settlement settlement = createCompletedSettlement(5L, 30L, 200000L, 6000L, 10000L, 184000L);
            when(findSettlementPort.findById(5L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(5L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase).record(captor.capture());

            List<RecordLedgerEntryCommand.EntryLine> entries = captor.getValue().entries();

            RecordLedgerEntryCommand.EntryLine debitPgReceivable = findEntry(entries, PG_RECEIVABLE_ID, TransactionType.DEBIT);
            assertThat(debitPgReceivable.amount()).isEqualTo(200000L);

            RecordLedgerEntryCommand.EntryLine creditCash = findEntry(entries, CASH_ID, TransactionType.CREDIT);
            assertThat(creditCash.amount()).isEqualTo(194000L);

            RecordLedgerEntryCommand.EntryLine creditPgFee = findEntry(entries, PG_FEE_ID, TransactionType.CREDIT);
            assertThat(creditPgFee.amount()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("통합 분개에는 Seller 역분개 3개(DEBIT 보통예금, DEBIT 플랫폼수수료수익, CREDIT 미지급금_판매자) 항목이 포함된다")
        void shouldIncludeSellerReversalEntries() {
            // given
            Settlement settlement = createCompletedSettlement(5L, 30L, 200000L, 6000L, 10000L, 184000L);
            when(findSettlementPort.findById(5L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(5L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase).record(captor.capture());

            List<RecordLedgerEntryCommand.EntryLine> entries = captor.getValue().entries();

            RecordLedgerEntryCommand.EntryLine debitCash = findEntry(entries, CASH_ID, TransactionType.DEBIT);
            assertThat(debitCash.amount()).isEqualTo(184000L);

            RecordLedgerEntryCommand.EntryLine debitPlatformFee = findEntry(entries, PLATFORM_FEE_ID, TransactionType.DEBIT);
            assertThat(debitPlatformFee.amount()).isEqualTo(10000L);

            RecordLedgerEntryCommand.EntryLine creditSellerPayable = findEntry(entries, SELLER_PAYABLE_ID, TransactionType.CREDIT);
            assertThat(creditSellerPayable.amount()).isEqualTo(194000L);
        }
    }
}
