package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.*;
import com.personal.marketnote.commerce.exception.NoUnsettledAllocationException;
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
@DisplayName("ExecuteSettlementUseCase 테스트")
class ExecuteSettlementUseCaseTest {

    @InjectMocks
    private ExecuteSettlementService executeSettlementService;

    @Mock
    private FindPaymentAllocationPort findPaymentAllocationPort;

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
    @DisplayName("정상 정산 실행")
    class SuccessfulSettlement {

        @Test
        @DisplayName("단일 판매자 정산을 정상 처리한다")
        void shouldExecuteSettlementForSingleSeller() {
            // given
            PaymentAllocation allocation1 = createAllocation(1L, 10L, 5000L);
            PaymentAllocation allocation2 = createAllocation(2L, 10L, 3000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(allocation1, allocation2));
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
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(saveSettlementPort).save(settlementCaptor.capture());
            Settlement savedSettlement = settlementCaptor.getValue();
            assertThat(savedSettlement.getSellerId()).isEqualTo(10L);
            assertThat(savedSettlement.getTotalAllocatedAmount()).isEqualTo(8000L);
            assertThat(savedSettlement.getPgFeeAmount()).isEqualTo(240L);      // 8000 * 300 / 10000
            assertThat(savedSettlement.getPlatformFeeAmount()).isEqualTo(400L); // 8000 * 500 / 10000
            assertThat(savedSettlement.getSellerPayoutAmount()).isEqualTo(7360L); // 8000 - 240 - 400

            verify(updatePaymentAllocationPort).assignSettlement(allocationIdsCaptor.capture(), eq(1L));
            assertThat(allocationIdsCaptor.getValue()).containsExactlyInAnyOrder(1L, 2L);

            verify(recordLedgerEntryUseCase).recordPgSettlement(1L, 8000L, 240L);
            verify(recordLedgerEntryUseCase).recordSellerSettlement(1L, 7760L, 7360L, 400L); // 7760 = 7360 + 400
            verify(updateSettlementPort).update(argThat(Settlement::isCompleted));
        }

        @Test
        @DisplayName("다중 판매자 정산 시 각각 독립 Settlement를 생성한다")
        void shouldExecuteSettlementForMultipleSellers() {
            // given
            PaymentAllocation seller10Alloc = createAllocation(1L, 10L, 10000L);
            PaymentAllocation seller20Alloc = createAllocation(2L, 20L, 20000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(seller10Alloc, seller20Alloc));
            when(findSettlementPort.existsBySellerIdAndYearAndMonth(anyLong(), eq(2026), eq(2)))
                    .thenReturn(false);
            when(saveSettlementPort.save(any(Settlement.class)))
                    .thenAnswer(invocation -> {
                        Settlement s = invocation.getArgument(0);
                        Long id = s.getSellerId().equals(10L) ? 1L : 2L;
                        return createSavedSettlement(id, s.getSellerId(), s.getYear(), s.getMonth(),
                                s.getTotalAllocatedAmount(), s.getPgFeeAmount(),
                                s.getPlatformFeeAmount(), s.getSellerPayoutAmount());
                    });
            when(updateSettlementPort.update(any(Settlement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(saveSettlementPort, times(2)).save(any(Settlement.class));
            verify(recordLedgerEntryUseCase, times(2)).recordPgSettlement(anyLong(), anyLong(), anyLong());
            verify(recordLedgerEntryUseCase, times(2)).recordSellerSettlement(anyLong(), anyLong(), anyLong(), anyLong());
            verify(updateSettlementPort, times(2)).update(argThat(Settlement::isCompleted));
        }

        @Test
        @DisplayName("PG 수수료가 0인 경우 정상 처리한다")
        void shouldHandleZeroPgFee() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 10000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(allocation));
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
                    .year(2026).month(2).pgFeeRate(0).platformFeeRate(500).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(saveSettlementPort).save(settlementCaptor.capture());
            Settlement saved = settlementCaptor.getValue();
            assertThat(saved.getPgFeeAmount()).isEqualTo(0L);
            assertThat(saved.getPlatformFeeAmount()).isEqualTo(500L);
            assertThat(saved.getSellerPayoutAmount()).isEqualTo(9500L);

            verify(recordLedgerEntryUseCase).recordPgSettlement(1L, 10000L, 0L);
        }
    }

    @Nested
    @DisplayName("수수료 계산 정합성")
    class FeeCalculation {

        @Test
        @DisplayName("수수료 역산 정합성: totalAllocated = pgFee + platformFee + sellerPayout")
        void shouldMaintainFeeIntegrity() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 10001L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(allocation));
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
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(saveSettlementPort).save(settlementCaptor.capture());
            Settlement saved = settlementCaptor.getValue();

            long pgFee = saved.getPgFeeAmount();
            long platformFee = saved.getPlatformFeeAmount();
            long sellerPayout = saved.getSellerPayoutAmount();
            long total = saved.getTotalAllocatedAmount();

            // 역산 정합성: pgFee + platformFee + sellerPayout == totalAllocatedAmount
            assertThat(pgFee + platformFee + sellerPayout).isEqualTo(total);

            // basis point 내림: 10001 * 300 / 10000 = 300 (not 300.03)
            assertThat(pgFee).isEqualTo(300L);
            // 10001 * 500 / 10000 = 500
            assertThat(platformFee).isEqualTo(500L);
            // 10001 - 300 - 500 = 9201
            assertThat(sellerPayout).isEqualTo(9201L);
        }

        @Test
        @DisplayName("수수료율 합계가 100%일 때 판매자 지급액이 0이 되지 않고 정합성 유지")
        void shouldHandleHighFeeRates() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 10000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(allocation));
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

            // pgFeeRate=3000 (30%) + platformFeeRate=2000 (20%) = 50%
            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(3000).platformFeeRate(2000).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(saveSettlementPort).save(settlementCaptor.capture());
            Settlement saved = settlementCaptor.getValue();
            assertThat(saved.getPgFeeAmount()).isEqualTo(3000L);
            assertThat(saved.getPlatformFeeAmount()).isEqualTo(2000L);
            assertThat(saved.getSellerPayoutAmount()).isEqualTo(5000L);
            assertThat(saved.getPgFeeAmount() + saved.getPlatformFeeAmount() + saved.getSellerPayoutAmount())
                    .isEqualTo(saved.getTotalAllocatedAmount());
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("미정산 배분이 없으면 NoUnsettledAllocationException을 던진다")
        void shouldThrowWhenNoUnsettledAllocations() {
            // given
            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of());

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when & then
            assertThatThrownBy(() -> executeSettlementService.executeSettlement(command))
                    .isInstanceOf(NoUnsettledAllocationException.class);

            verify(saveSettlementPort, never()).save(any());
        }

        @Test
        @DisplayName("이미 해당 기간 정산이 존재하면 SettlementAlreadyExistsException을 던진다")
        void shouldThrowWhenSettlementAlreadyExists() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 10000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(allocation));
            when(findSettlementPort.existsBySellerIdAndYearAndMonth(10L, 2026, 2))
                    .thenReturn(true);

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when & then
            assertThatThrownBy(() -> executeSettlementService.executeSettlement(command))
                    .isInstanceOf(SettlementAlreadyExistsException.class)
                    .hasMessageContaining("10")
                    .hasMessageContaining("2026")
                    .hasMessageContaining("2");

            verify(saveSettlementPort, never()).save(any());
        }

        @Test
        @DisplayName("PG 수수료율이 음수이면 IllegalArgumentException을 던진다")
        void shouldThrowWhenNegativePgFeeRate() {
            // given
            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(-1).platformFeeRate(500).build();

            // when & then
            assertThatThrownBy(() -> executeSettlementService.executeSettlement(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PG 수수료율");
        }

        @Test
        @DisplayName("수수료율 합계가 100%를 초과하면 IllegalArgumentException을 던진다")
        void shouldThrowWhenFeeRatesExceed100Percent() {
            // given
            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(6000).platformFeeRate(5000).build();

            // when & then
            assertThatThrownBy(() -> executeSettlementService.executeSettlement(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("100%");
        }
    }

    @Nested
    @DisplayName("PaymentAllocation 정산 할당")
    class AllocationAssignment {

        @Test
        @DisplayName("정산 완료 후 PaymentAllocation에 settlementId를 할당한다")
        void shouldAssignSettlementIdToAllocations() {
            // given
            PaymentAllocation alloc1 = createAllocation(1L, 10L, 3000L);
            PaymentAllocation alloc2 = createAllocation(2L, 10L, 7000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(alloc1, alloc2));
            when(findSettlementPort.existsBySellerIdAndYearAndMonth(10L, 2026, 2))
                    .thenReturn(false);
            when(saveSettlementPort.save(any(Settlement.class)))
                    .thenAnswer(invocation -> {
                        Settlement s = invocation.getArgument(0);
                        return createSavedSettlement(99L, s.getSellerId(), s.getYear(), s.getMonth(),
                                s.getTotalAllocatedAmount(), s.getPgFeeAmount(),
                                s.getPlatformFeeAmount(), s.getSellerPayoutAmount());
                    });
            when(updateSettlementPort.update(any(Settlement.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(updatePaymentAllocationPort).assignSettlement(allocationIdsCaptor.capture(), eq(99L));
            assertThat(allocationIdsCaptor.getValue()).containsExactlyInAnyOrder(1L, 2L);
        }
    }
}
