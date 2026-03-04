package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.InvalidSettlementStatusTransitionException;
import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetryFailedSettlementUseCase 테스트")
class RetryFailedSettlementUseCaseTest {

    @InjectMocks
    private RetryFailedSettlementService retryFailedSettlementService;

    @Mock
    private FindSettlementPort findSettlementPort;

    @Mock
    private UpdateSettlementPort updateSettlementPort;

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    private Settlement createFailedSettlement(Long id, Long sellerId, Long totalAllocatedAmount,
                                              Long pgFeeAmount, Long platformFeeAmount, Long sellerPayoutAmount) {
        Settlement settlement = Settlement.from(SettlementSnapshotState.builder()
                .id(id)
                .sellerId(sellerId)
                .year(2026)
                .month(2)
                .totalAllocatedAmount(totalAllocatedAmount)
                .pgFeeAmount(pgFeeAmount)
                .platformFeeAmount(platformFeeAmount)
                .sellerPayoutAmount(sellerPayoutAmount)
                .status(SettlementStatus.FAILED)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
        return settlement;
    }

    @Nested
    @DisplayName("재시도 성공")
    class RetrySuccess {

        @Test
        @DisplayName("실패한 정산을 재시도하면 분개를 기록하고 COMPLETED 상태로 전이한다")
        void shouldRetryFailedSettlementSuccessfully() {
            // given
            Settlement failedSettlement = createFailedSettlement(1L, 10L, 100000L, 3000L, 5000L, 92000L);

            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(failedSettlement));
            when(updateSettlementPort.update(any(Settlement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            retryFailedSettlementService.retrySettlement(1L);

            // then
            verify(recordLedgerEntryUseCase).recordPgSettlement(1L, 100000L, 3000L);
            verify(recordLedgerEntryUseCase).recordSellerSettlement(1L, 97000L, 92000L, 5000L);
            verify(updateSettlementPort).update(argThat(Settlement::isCompleted));
        }

        @Test
        @DisplayName("PG 수수료가 0인 실패 정산을 재시도한다")
        void shouldRetryWithZeroPgFee() {
            // given
            Settlement failedSettlement = createFailedSettlement(2L, 20L, 50000L, 0L, 2500L, 47500L);

            when(findSettlementPort.findById(2L)).thenReturn(Optional.of(failedSettlement));
            when(updateSettlementPort.update(any(Settlement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            retryFailedSettlementService.retrySettlement(2L);

            // then
            verify(recordLedgerEntryUseCase).recordPgSettlement(2L, 50000L, 0L);
            verify(recordLedgerEntryUseCase).recordSellerSettlement(2L, 50000L, 47500L, 2500L);
            verify(updateSettlementPort).update(argThat(Settlement::isCompleted));
        }
    }

    @Nested
    @DisplayName("재시도 실패")
    class RetryFailure {

        @Test
        @DisplayName("존재하지 않는 정산 ID로 재시도하면 SettlementNotFoundException을 던진다")
        void shouldThrowWhenSettlementNotFound() {
            // given
            when(findSettlementPort.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> retryFailedSettlementService.retrySettlement(999L))
                    .isInstanceOf(SettlementNotFoundException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("PENDING 상태의 정산에 대해 재시도하면 InvalidSettlementStatusTransitionException을 던진다")
        void shouldThrowWhenSettlementIsPending() {
            // given
            Settlement pendingSettlement = Settlement.from(SettlementSnapshotState.builder()
                    .id(1L)
                    .sellerId(10L)
                    .year(2026)
                    .month(2)
                    .totalAllocatedAmount(100000L)
                    .pgFeeAmount(3000L)
                    .platformFeeAmount(5000L)
                    .sellerPayoutAmount(92000L)
                    .status(SettlementStatus.PENDING)
                    .version(0L)
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .build());

            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(pendingSettlement));

            // when & then
            assertThatThrownBy(() -> retryFailedSettlementService.retrySettlement(1L))
                    .isInstanceOf(InvalidSettlementStatusTransitionException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }

        @Test
        @DisplayName("COMPLETED 상태의 정산에 대해 재시도하면 InvalidSettlementStatusTransitionException을 던진다")
        void shouldThrowWhenSettlementIsCompleted() {
            // given
            Settlement completedSettlement = Settlement.from(SettlementSnapshotState.builder()
                    .id(1L)
                    .sellerId(10L)
                    .year(2026)
                    .month(2)
                    .totalAllocatedAmount(100000L)
                    .pgFeeAmount(3000L)
                    .platformFeeAmount(5000L)
                    .sellerPayoutAmount(92000L)
                    .status(SettlementStatus.COMPLETED)
                    .version(1L)
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .build());

            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(completedSettlement));

            // when & then
            assertThatThrownBy(() -> retryFailedSettlementService.retrySettlement(1L))
                    .isInstanceOf(InvalidSettlementStatusTransitionException.class);

            verifyNoInteractions(recordLedgerEntryUseCase);
            verifyNoInteractions(updateSettlementPort);
        }
    }

    @Nested
    @DisplayName("분개 기록 검증")
    class LedgerVerification {

        @Test
        @DisplayName("재시도 시 분개 금액이 기존 정산 금액과 일치한다")
        void shouldRecordLedgerWithCorrectAmounts() {
            // given
            Settlement failedSettlement = createFailedSettlement(5L, 30L, 200000L, 6000L, 10000L, 184000L);

            when(findSettlementPort.findById(5L)).thenReturn(Optional.of(failedSettlement));
            when(updateSettlementPort.update(any(Settlement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            retryFailedSettlementService.retrySettlement(5L);

            // then
            // PG 정산: settlementId, totalAllocatedAmount, pgFeeAmount
            verify(recordLedgerEntryUseCase).recordPgSettlement(5L, 200000L, 6000L);

            // 판매자 정산: settlementId, debit(sellerPayout+platformFee), sellerPayout, platformFee
            verify(recordLedgerEntryUseCase).recordSellerSettlement(5L, 194000L, 184000L, 10000L);
        }
    }
}
