package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.*;
import com.personal.marketnote.commerce.exception.NoUnsettledAllocationException;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

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
    private ProcessSellerSettlementService processSellerSettlementService;

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

    @Nested
    @DisplayName("정상 정산 실행")
    class SuccessfulSettlement {

        @Test
        @DisplayName("단일 판매자 정산 시 ProcessSellerSettlementService를 호출한다")
        void shouldDelegateToProcessService() {
            // given
            PaymentAllocation allocation1 = createAllocation(1L, 10L, 5000L);
            PaymentAllocation allocation2 = createAllocation(2L, 10L, 3000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(allocation1, allocation2));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService).process(eq(command), eq(10L), anyList());
        }

        @Test
        @DisplayName("다중 판매자 정산 시 각 판매자별로 ProcessSellerSettlementService를 호출한다")
        void shouldProcessEachSellerIndependently() {
            // given
            PaymentAllocation seller10Alloc = createAllocation(1L, 10L, 10000L);
            PaymentAllocation seller20Alloc = createAllocation(2L, 20L, 20000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(seller10Alloc, seller20Alloc));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService, times(2)).process(eq(command), anyLong(), anyList());
        }
    }

    @Nested
    @DisplayName("부분 실패 처리")
    class PartialFailureHandling {

        @Test
        @DisplayName("한 판매자 정산 실패 시 나머지 판매자 정산은 계속 진행된다")
        void shouldContinueProcessingWhenOneSellerFails() {
            // given
            PaymentAllocation seller10Alloc = createAllocation(1L, 10L, 10000L);
            PaymentAllocation seller20Alloc = createAllocation(2L, 20L, 20000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(seller10Alloc, seller20Alloc));

            doThrow(new RuntimeException("분개 기록 실패"))
                    .doNothing()
                    .when(processSellerSettlementService).process(any(), anyLong(), anyList());

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService, times(2)).process(eq(command), anyLong(), anyList());
        }

        @Test
        @DisplayName("모든 판매자 정산이 실패해도 예외를 던지지 않는다")
        void shouldNotThrowWhenAllSellersFail() {
            // given
            PaymentAllocation seller10Alloc = createAllocation(1L, 10L, 10000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(seller10Alloc));

            doThrow(new RuntimeException("분개 기록 실패"))
                    .when(processSellerSettlementService).process(any(), anyLong(), anyList());

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).pgFeeRate(300).platformFeeRate(500).build();

            // when - 예외 없이 완료
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService).process(eq(command), eq(10L), anyList());
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

            verifyNoInteractions(processSellerSettlementService);
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
}
