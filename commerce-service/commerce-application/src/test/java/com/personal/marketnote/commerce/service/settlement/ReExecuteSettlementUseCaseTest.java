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
@DisplayName("ReExecuteSettlementUseCase 테스트")
class ReExecuteSettlementUseCaseTest {

    @InjectMocks
    private ReExecuteSettlementService reExecuteSettlementService;

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

    private Settlement createCancelledSettlement(Long id, Long sellerId, Long totalAllocatedAmount,
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
                .status(SettlementStatus.CANCELLED)
                .version(2L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    @Nested
    @DisplayName("재실행 성공")
    class ReExecuteSuccess {

        @Test
        @DisplayName("취소된 정산을 재실행하면 분개를 기록하고 COMPLETED 상태로 전이한다")
        void shouldReExecuteCancelledSettlementSuccessfully() {
            // given
            Settlement cancelledSettlement = createCancelledSettlement(1L, 10L, 100000L, 3000L, 5000L, 92000L);
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(cancelledSettlement));

            // when
            reExecuteSettlementService.reExecuteSettlement(1L);

            // then
            verify(recordLedgerEntryUseCase, times(2)).record(any(RecordLedgerEntryCommand.class));
            verify(updateSettlementPort).update(argThat(Settlement::isCompleted));
        }

        @Test
        @DisplayName("PG 수수료가 0인 취소 정산을 재실행한다")
        void shouldReExecuteWithZeroPgFee() {
            // given
            Settlement settlement = createCancelledSettlement(2L, 20L, 50000L, 0L, 2500L, 47500L);
            when(findSettlementPort.findById(2L)).thenReturn(Optional.of(settlement));

            // when
            reExecuteSettlementService.reExecuteSettlement(2L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase, times(2)).record(captor.capture());

            RecordLedgerEntryCommand pgCommand = captor.getAllValues().get(0);
            assertThat(pgCommand.entries()).hasSize(2);
            assertThat(pgCommand.idempotencyKey()).isEqualTo("PG_SETTLEMENT_REEXEC:2");
        }

        @Test
        @DisplayName("플랫폼 수수료가 0인 취소 정산을 재실행한다")
        void shouldReExecuteWithZeroPlatformFee() {
            // given
            Settlement settlement = createCancelledSettlement(3L, 30L, 80000L, 4000L, 0L, 76000L);
            when(findSettlementPort.findById(3L)).thenReturn(Optional.of(settlement));

            // when
            reExecuteSettlementService.reExecuteSettlement(3L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase, times(2)).record(captor.capture());

            RecordLedgerEntryCommand sellerCommand = captor.getAllValues().get(1);
            assertThat(sellerCommand.entries()).hasSize(2);
            assertThat(sellerCommand.idempotencyKey()).isEqualTo("SELLER_SETTLEMENT_REEXEC:3");
        }
    }

    @Nested
    @DisplayName("재실행 실패")
    class ReExecuteFailure {

        @Test
        @DisplayName("존재하지 않는 정산 ID로 재실행하면 SettlementNotFoundException을 던진다")
        void shouldThrowWhenSettlementNotFound() {
            // given
            when(findSettlementPort.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reExecuteSettlementService.reExecuteSettlement(999L))
                    .isInstanceOf(SettlementNotFoundException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("PENDING 상태의 정산을 재실행하면 InvalidSettlementStatusTransitionException을 던진다")
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
            assertThatThrownBy(() -> reExecuteSettlementService.reExecuteSettlement(1L))
                    .isInstanceOf(InvalidSettlementStatusTransitionException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("COMPLETED 상태의 정산을 재실행하면 InvalidSettlementStatusTransitionException을 던진다")
        void shouldThrowWhenSettlementIsCompleted() {
            // given
            Settlement completedSettlement = Settlement.from(SettlementSnapshotState.builder()
                    .id(1L).sellerId(10L).year(2026).month(2)
                    .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                    .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                    .status(SettlementStatus.COMPLETED).version(1L)
                    .createdAt(LocalDateTime.now()).modifiedAt(LocalDateTime.now())
                    .build());
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(completedSettlement));

            // when & then
            assertThatThrownBy(() -> reExecuteSettlementService.reExecuteSettlement(1L))
                    .isInstanceOf(InvalidSettlementStatusTransitionException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("FAILED 상태의 정산을 재실행하면 InvalidSettlementStatusTransitionException을 던진다")
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
            assertThatThrownBy(() -> reExecuteSettlementService.reExecuteSettlement(1L))
                    .isInstanceOf(InvalidSettlementStatusTransitionException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }
    }

    @Nested
    @DisplayName("분개 실패 시 FAILED 상태 전이")
    class LedgerFailure {

        @Test
        @DisplayName("분개 기록 중 예외 발생 시 FAILED 상태로 저장하고 예외를 재throw한다")
        void shouldTransitionToFailedOnLedgerException() {
            // given
            Settlement cancelledSettlement = createCancelledSettlement(1L, 10L, 100000L, 3000L, 5000L, 92000L);
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(cancelledSettlement));
            doThrow(new RuntimeException("분개 기록 실패"))
                    .when(recordLedgerEntryUseCase).record(any(RecordLedgerEntryCommand.class));

            // when & then
            assertThatThrownBy(() -> reExecuteSettlementService.reExecuteSettlement(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("분개 기록 실패");

            verify(updateSettlementPort).update(argThat(Settlement::isFailed));
        }
    }

    @Nested
    @DisplayName("분개 검증")
    class LedgerVerification {

        @Test
        @DisplayName("재실행 분개의 DEBIT/CREDIT이 원래 정산과 동일하다")
        void shouldRecordLedgerWithCorrectAmounts() {
            // given
            Settlement settlement = createCancelledSettlement(5L, 30L, 200000L, 6000L, 10000L, 184000L);
            when(findSettlementPort.findById(5L)).thenReturn(Optional.of(settlement));

            // when
            reExecuteSettlementService.reExecuteSettlement(5L);

            // then
            ArgumentCaptor<RecordLedgerEntryCommand> captor = ArgumentCaptor.forClass(RecordLedgerEntryCommand.class);
            verify(recordLedgerEntryUseCase, times(2)).record(captor.capture());

            // PG 정산 분개
            RecordLedgerEntryCommand pgCommand = captor.getAllValues().get(0);
            assertThat(pgCommand.transactionType()).isEqualTo(LedgerTransactionType.PG_SETTLEMENT);
            assertThat(pgCommand.idempotencyKey()).isEqualTo("PG_SETTLEMENT_REEXEC:5");
            assertThat(pgCommand.entries()).hasSize(3);

            // DEBIT 보통예금 = 194000
            assertThat(pgCommand.entries().get(0).accountId()).isEqualTo(CASH_ID);
            assertThat(pgCommand.entries().get(0).amount()).isEqualTo(194000L);
            assertThat(pgCommand.entries().get(0).transactionType()).isEqualTo(TransactionType.DEBIT);

            // DEBIT PG수수료비용 = 6000
            assertThat(pgCommand.entries().get(1).accountId()).isEqualTo(PG_FEE_ID);
            assertThat(pgCommand.entries().get(1).amount()).isEqualTo(6000L);
            assertThat(pgCommand.entries().get(1).transactionType()).isEqualTo(TransactionType.DEBIT);

            // CREDIT 매출채권_PG = 200000
            assertThat(pgCommand.entries().get(2).accountId()).isEqualTo(PG_RECEIVABLE_ID);
            assertThat(pgCommand.entries().get(2).amount()).isEqualTo(200000L);
            assertThat(pgCommand.entries().get(2).transactionType()).isEqualTo(TransactionType.CREDIT);

            // 판매자 정산 분개
            RecordLedgerEntryCommand sellerCommand = captor.getAllValues().get(1);
            assertThat(sellerCommand.transactionType()).isEqualTo(LedgerTransactionType.SELLER_SETTLEMENT);
            assertThat(sellerCommand.idempotencyKey()).isEqualTo("SELLER_SETTLEMENT_REEXEC:5");
            assertThat(sellerCommand.entries()).hasSize(3);

            // DEBIT 미지급금_판매자 = 194000
            assertThat(sellerCommand.entries().get(0).accountId()).isEqualTo(SELLER_PAYABLE_ID);
            assertThat(sellerCommand.entries().get(0).amount()).isEqualTo(194000L);
            assertThat(sellerCommand.entries().get(0).transactionType()).isEqualTo(TransactionType.DEBIT);

            // CREDIT 보통예금 = 184000
            assertThat(sellerCommand.entries().get(1).accountId()).isEqualTo(CASH_ID);
            assertThat(sellerCommand.entries().get(1).amount()).isEqualTo(184000L);
            assertThat(sellerCommand.entries().get(1).transactionType()).isEqualTo(TransactionType.CREDIT);

            // CREDIT 플랫폼수수료수익 = 10000
            assertThat(sellerCommand.entries().get(2).accountId()).isEqualTo(PLATFORM_FEE_ID);
            assertThat(sellerCommand.entries().get(2).amount()).isEqualTo(10000L);
            assertThat(sellerCommand.entries().get(2).transactionType()).isEqualTo(TransactionType.CREDIT);
        }
    }
}
