package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.*;
import com.personal.marketnote.commerce.exception.SettlementAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.in.usecase.ledger.RecordLedgerEntryUseCase;
import com.personal.marketnote.commerce.port.out.settlement.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessSellerSettlementService 테스트")
class ProcessSellerSettlementServiceTest {

    @InjectMocks
    private ProcessSellerSettlementService processSellerSettlementService;

    @Mock
    private FindSettlementPort findSettlementPort;

    @Mock
    private SaveSettlementPort saveSettlementPort;

    @Mock
    private UpdateSettlementPort updateSettlementPort;

    @Mock
    private UpdatePaymentAllocationPort updatePaymentAllocationPort;

    @Mock
    private RecordLedgerEntryUseCase recordLedgerEntryUseCase;

    @Captor
    private ArgumentCaptor<Settlement> settlementCaptor;

    @Captor
    private ArgumentCaptor<List<Long>> allocationIdsCaptor;

    private PaymentAllocation createAllocation(Long id, Long sellerId, Long allocatedAmount) {
        return PaymentAllocation.from(PaymentAllocationSnapshotState.builder()
                .id(id)
                .orderId(100L)
                .sellerId(sellerId)
                .allocatedAmount(allocatedAmount)
                .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                .targetType(PaymentAllocationTargetType.ORDER)
                .idempotencyKey("TEST:" + id)
                .createdAt(LocalDateTime.of(2026, 2, 15, 10, 0))
                .build());
    }

    private Settlement createSavedSettlement(Long id, Long sellerId, Integer year, Integer month,
                                             Long totalAllocatedAmount, Long pgFeeAmount,
                                             Long platformFeeAmount, Long sellerPayoutAmount) {
        return Settlement.from(SettlementSnapshotState.builder()
                .id(id)
                .sellerId(sellerId)
                .year(year)
                .month(month)
                .totalAllocatedAmount(totalAllocatedAmount)
                .pgFeeAmount(pgFeeAmount)
                .platformFeeAmount(platformFeeAmount)
                .sellerPayoutAmount(sellerPayoutAmount)
                .status(SettlementStatus.PENDING)
                .version(0L)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
    }

    @Nested
    @DisplayName("정상 처리")
    class SuccessfulProcess {

        @Test
        @DisplayName("판매자 정산을 정상 처리한다")
        void shouldProcessSellerSettlement() {
            // given
            PaymentAllocation allocation1 = createAllocation(1L, 10L, 5000L);
            PaymentAllocation allocation2 = createAllocation(2L, 10L, 3000L);

            when(findSettlementPort.existsBySellerIdAndYearAndMonth(10L, 2026, 2))
                    .thenReturn(false);
            when(saveSettlementPort.save(any(Settlement.class)))
                    .thenAnswer(invocation -> {
                        Settlement s = invocation.getArgument(0);
                        return createSavedSettlement(1L, s.getSellerId(), s.getYear(), s.getMonth(),
                                s.getTotalAllocatedAmount(), s.getPgFeeAmount(),
                                s.getPlatformFeeAmount(), s.getSellerPayoutAmount());
                    });
            when(updateSettlementPort.update(any(Settlement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when
            processSellerSettlementService.process(command, 10L, List.of(allocation1, allocation2), 300, 500);

            // then
            verify(saveSettlementPort).save(settlementCaptor.capture());
            Settlement savedSettlement = settlementCaptor.getValue();
            assertThat(savedSettlement.getSellerId()).isEqualTo(10L);
            assertThat(savedSettlement.getTotalAllocatedAmount()).isEqualTo(8000L);
            assertThat(savedSettlement.getPgFeeAmount()).isEqualTo(240L);
            assertThat(savedSettlement.getPlatformFeeAmount()).isEqualTo(400L);
            assertThat(savedSettlement.getSellerPayoutAmount()).isEqualTo(7360L);

            verify(updatePaymentAllocationPort).assignSettlement(allocationIdsCaptor.capture(), eq(1L));
            assertThat(allocationIdsCaptor.getValue()).containsExactlyInAnyOrder(1L, 2L);

            verify(recordLedgerEntryUseCase).recordPgSettlement(1L, 8000L, 240L);
            verify(recordLedgerEntryUseCase).recordSellerSettlement(1L, 7760L, 7360L, 400L);
            verify(updateSettlementPort).update(argThat(Settlement::isCompleted));
        }

        @Test
        @DisplayName("PG 수수료가 0인 경우 정상 처리한다")
        void shouldHandleZeroPgFee() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 10000L);

            when(findSettlementPort.existsBySellerIdAndYearAndMonth(10L, 2026, 2))
                    .thenReturn(false);
            when(saveSettlementPort.save(any(Settlement.class)))
                    .thenAnswer(invocation -> {
                        Settlement s = invocation.getArgument(0);
                        return createSavedSettlement(1L, s.getSellerId(), s.getYear(), s.getMonth(),
                                s.getTotalAllocatedAmount(), s.getPgFeeAmount(),
                                s.getPlatformFeeAmount(), s.getSellerPayoutAmount());
                    });
            when(updateSettlementPort.update(any(Settlement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when
            processSellerSettlementService.process(command, 10L, List.of(allocation), 0, 500);

            // then
            verify(saveSettlementPort).save(settlementCaptor.capture());
            Settlement saved = settlementCaptor.getValue();
            assertThat(saved.getPgFeeAmount()).isEqualTo(0L);
            assertThat(saved.getSellerPayoutAmount()).isEqualTo(9500L);
        }

        @Test
        @DisplayName("수수료 역산 정합성이 유지된다")
        void shouldMaintainFeeIntegrity() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 10001L);

            when(findSettlementPort.existsBySellerIdAndYearAndMonth(10L, 2026, 2))
                    .thenReturn(false);
            when(saveSettlementPort.save(any(Settlement.class)))
                    .thenAnswer(invocation -> {
                        Settlement s = invocation.getArgument(0);
                        return createSavedSettlement(1L, s.getSellerId(), s.getYear(), s.getMonth(),
                                s.getTotalAllocatedAmount(), s.getPgFeeAmount(),
                                s.getPlatformFeeAmount(), s.getSellerPayoutAmount());
                    });
            when(updateSettlementPort.update(any(Settlement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when
            processSellerSettlementService.process(command, 10L, List.of(allocation), 300, 500);

            // then
            verify(saveSettlementPort).save(settlementCaptor.capture());
            Settlement saved = settlementCaptor.getValue();

            long pgFee = saved.getPgFeeAmount();
            long platformFee = saved.getPlatformFeeAmount();
            long sellerPayout = saved.getSellerPayoutAmount();
            long total = saved.getTotalAllocatedAmount();

            assertThat(pgFee + platformFee + sellerPayout).isEqualTo(total);
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("이미 해당 기간 정산이 존재하면 SettlementAlreadyExistsException을 던진다")
        void shouldThrowWhenSettlementAlreadyExists() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 10000L);

            when(findSettlementPort.existsBySellerIdAndYearAndMonth(10L, 2026, 2))
                    .thenReturn(true);

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when & then
            assertThatThrownBy(() -> processSellerSettlementService.process(
                    command, 10L, List.of(allocation), 300, 500))
                    .isInstanceOf(SettlementAlreadyExistsException.class);

            verify(saveSettlementPort, never()).save(any());
        }
    }
}
