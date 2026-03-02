package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.ledger.*;
import com.personal.marketnote.commerce.domain.settlement.*;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.command.ledger.RecordLedgerEntryCommand;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.out.ledger.FindAccountPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPort;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

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
        lenient().when(findAccountPort.findByName("매출채권_PG")).thenReturn(Optional.of(createAccount(PG_RECEIVABLE_ID, "매출채권_PG")));
        lenient().when(findAccountPort.findByName("미지급금_판매자")).thenReturn(Optional.of(createAccount(SELLER_PAYABLE_ID, "미지급금_판매자")));
        lenient().when(findAccountPort.findByName("보통예금")).thenReturn(Optional.of(createAccount(CASH_ID, "보통예금")));
        lenient().when(findAccountPort.findByName("PG수수료비용")).thenReturn(Optional.of(createAccount(PG_FEE_ID, "PG수수료비용")));
        lenient().when(findAccountPort.findByName("플랫폼수수료수익")).thenReturn(Optional.of(createAccount(PLATFORM_FEE_ID, "플랫폼수수료수익")));
    }

    private Account createAccount(Long id, String name) {
        return Account.from(AccountSnapshotState.builder()
                .id(id)
                .name(name)
                .accountType(AccountType.ASSET)
                .status(com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    private Settlement createCompletedSettlement(Long id, Long sellerId, Long totalAllocatedAmount,
                                                  Long pgFeeAmount, Long platformFeeAmount, Long sellerPayoutAmount) {
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
                .build());
    }

    @Nested
    @DisplayName("취소 성공")
    class CancelSuccess {

        @Test
        @DisplayName("완료된 정산을 취소하면 역분개를 기록하고 CANCELLED 상태로 전이한다")
        void shouldCancelCompletedSettlementSuccessfully() {
            // given
            Settlement completedSettlement = createCompletedSettlement(1L, 10L, 100000L, 3000L, 5000L, 92000L);
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(completedSettlement));

            // when
            cancelSettlementService.cancelSettlement(1L);

            // then
            verify(recordLedgerEntryUseCase, times(2)).record(any(RecordLedgerEntryCommand.class));
            verify(updateSettlementPort).update(argThat(Settlement::isCancelled));
        }

        @Test
        @DisplayName("PG 수수료가 0인 정산을 취소하면 PG수수료비용 항목 없이 역분개를 기록한다")
        void shouldCancelWithZeroPgFee() {
            // given
            Settlement settlement = createCompletedSettlement(2L, 20L, 50000L, 0L, 2500L, 47500L);
            when(findSettlementPort.findById(2L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(2L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase, times(2)).record(captor.capture());

            RecordLedgerEntryCommand pgCommand = captor.getAllValues().get(0);
            assertThat(pgCommand.entries()).hasSize(2);
            assertThat(pgCommand.idempotencyKey()).isEqualTo("PG_SETTLEMENT_CANCEL:2");
        }

        @Test
        @DisplayName("플랫폼 수수료가 0인 정산을 취소하면 플랫폼수수료수익 항목 없이 역분개를 기록한다")
        void shouldCancelWithZeroPlatformFee() {
            // given
            Settlement settlement = createCompletedSettlement(3L, 30L, 80000L, 4000L, 0L, 76000L);
            when(findSettlementPort.findById(3L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(3L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase, times(2)).record(captor.capture());

            RecordLedgerEntryCommand sellerCommand = captor.getAllValues().get(1);
            assertThat(sellerCommand.entries()).hasSize(2);
            assertThat(sellerCommand.idempotencyKey()).isEqualTo("SELLER_SETTLEMENT_CANCEL:3");
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
        @DisplayName("PENDING 상태의 정산을 취소하면 InvalidSettlementStatusTransitionException을 던진다")
        void shouldThrowWhenSettlementIsPending() {
            // given
            Settlement pendingSettlement = Settlement.from(SettlementSnapshotState.builder()
                    .id(1L).sellerId(10L).year(2026).month(2)
                    .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                    .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                    .status(SettlementStatus.PENDING).version(0L)
                    .createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now())
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
    @DisplayName("역분개 검증")
    class ReverseLedgerVerification {

        @Test
        @DisplayName("PG 정산 역분개의 DEBIT/CREDIT이 원래 분개와 반대이다")
        void shouldRecordPgReversalWithCorrectAmounts() {
            // given
            Settlement settlement = createCompletedSettlement(5L, 30L, 200000L, 6000L, 10000L, 184000L);
            when(findSettlementPort.findById(5L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(5L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase, times(2)).record(captor.capture());

            RecordLedgerEntryCommand pgCommand = captor.getAllValues().get(0);
            assertThat(pgCommand.transactionType()).isEqualTo(LedgerTransactionType.SETTLEMENT_CANCELLATION);
            assertThat(pgCommand.idempotencyKey()).isEqualTo("PG_SETTLEMENT_CANCEL:5");
            assertThat(pgCommand.entries()).hasSize(3);

            // DEBIT 매출채권_PG = 200000
            RecordLedgerEntryCommand.EntryLine debit = pgCommand.entries().get(0);
            assertThat(debit.accountId()).isEqualTo(PG_RECEIVABLE_ID);
            assertThat(debit.amount()).isEqualTo(200000L);
            assertThat(debit.transactionType()).isEqualTo(TransactionType.DEBIT);

            // CREDIT 보통예금 = 194000
            RecordLedgerEntryCommand.EntryLine creditCash = pgCommand.entries().get(1);
            assertThat(creditCash.accountId()).isEqualTo(CASH_ID);
            assertThat(creditCash.amount()).isEqualTo(194000L);
            assertThat(creditCash.transactionType()).isEqualTo(TransactionType.CREDIT);

            // CREDIT PG수수료비용 = 6000
            RecordLedgerEntryCommand.EntryLine creditPgFee = pgCommand.entries().get(2);
            assertThat(creditPgFee.accountId()).isEqualTo(PG_FEE_ID);
            assertThat(creditPgFee.amount()).isEqualTo(6000L);
            assertThat(creditPgFee.transactionType()).isEqualTo(TransactionType.CREDIT);
        }

        @Test
        @DisplayName("판매자 정산 역분개의 DEBIT/CREDIT이 원래 분개와 반대이다")
        void shouldRecordSellerReversalWithCorrectAmounts() {
            // given
            Settlement settlement = createCompletedSettlement(5L, 30L, 200000L, 6000L, 10000L, 184000L);
            when(findSettlementPort.findById(5L)).thenReturn(Optional.of(settlement));

            // when
            cancelSettlementService.cancelSettlement(5L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase, times(2)).record(captor.capture());

            RecordLedgerEntryCommand sellerCommand = captor.getAllValues().get(1);
            assertThat(sellerCommand.transactionType()).isEqualTo(LedgerTransactionType.SETTLEMENT_CANCELLATION);
            assertThat(sellerCommand.idempotencyKey()).isEqualTo("SELLER_SETTLEMENT_CANCEL:5");
            assertThat(sellerCommand.entries()).hasSize(3);

            // DEBIT 보통예금 = 184000
            RecordLedgerEntryCommand.EntryLine debitCash = sellerCommand.entries().get(0);
            assertThat(debitCash.accountId()).isEqualTo(CASH_ID);
            assertThat(debitCash.amount()).isEqualTo(184000L);
            assertThat(debitCash.transactionType()).isEqualTo(TransactionType.DEBIT);

            // DEBIT 플랫폼수수료수익 = 10000
            RecordLedgerEntryCommand.EntryLine debitPlatformFee = sellerCommand.entries().get(1);
            assertThat(debitPlatformFee.accountId()).isEqualTo(PLATFORM_FEE_ID);
            assertThat(debitPlatformFee.amount()).isEqualTo(10000L);
            assertThat(debitPlatformFee.transactionType()).isEqualTo(TransactionType.DEBIT);

            // CREDIT 미지급금_판매자 = 194000
            RecordLedgerEntryCommand.EntryLine creditPayable = sellerCommand.entries().get(2);
            assertThat(creditPayable.accountId()).isEqualTo(SELLER_PAYABLE_ID);
            assertThat(creditPayable.amount()).isEqualTo(194000L);
            assertThat(creditPayable.transactionType()).isEqualTo(TransactionType.CREDIT);
        }
    }
}
