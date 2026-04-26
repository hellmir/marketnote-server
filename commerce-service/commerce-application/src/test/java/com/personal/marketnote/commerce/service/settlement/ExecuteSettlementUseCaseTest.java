package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.*;
import com.personal.marketnote.commerce.exception.NoUnsettledAllocationException;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.out.settlement.DefaultSettlementPolicyProvider;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private FindSettlementPolicyPort findSettlementPolicyPort;

    @Mock
    private DefaultSettlementPolicyProvider defaultSettlementPolicyProvider;

    @Mock
    private ProcessSellerSettlementService processSellerSettlementService;

    private PaymentAllocation createAllocation(Long id, Long sellerId, Long allocatedAmount) {
        return PaymentAllocation.from(PaymentAllocationSnapshotState.builder()
                .id(id)
                .orderId(100L)
                .sellerId(sellerId)
                .allocatedAmount(allocatedAmount)
                .shippingFee(0L)
                .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                .targetType(PaymentAllocationTargetType.ORDER)
                .idempotencyKey("TEST:" + id)
                .createdAt(LocalDateTime.of(2026, 2, 15, 10, 0))
                .build());
    }

    private SettlementPolicy createPolicy(Long sellerId, Integer pgFeeRate, Integer platformFeeRate) {
        return SettlementPolicy.from(SettlementPolicySnapshotState.builder()
                .id(1L)
                .sellerId(sellerId)
                .pgFeeRate(pgFeeRate)
                .platformFeeRate(platformFeeRate)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(0L)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build());
    }

    @Nested
    @DisplayName("정상 정산 실행")
    class SuccessfulSettlement {

        @BeforeEach
        void setUp() {
            lenient().when(defaultSettlementPolicyProvider.getDefaultPgFeeRate()).thenReturn(300);
            lenient().when(defaultSettlementPolicyProvider.getDefaultPlatformFeeRate()).thenReturn(500);
        }

        @Test
        @DisplayName("판매자별 정산 정책이 없으면 기본 수수료율로 정산한다")
        void shouldUseDefaultFeeRatesWhenNoPolicyExists() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 5000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(allocation));
            when(findSettlementPolicyPort.findActiveBySellerIdIn(List.of(10L)))
                    .thenReturn(Map.of());

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService).process(eq(command), eq(10L), anyList(), eq(300), eq(500));
        }

        @Test
        @DisplayName("판매자별 정산 정책이 있으면 정책의 수수료율로 정산한다")
        void shouldUseSellerPolicyFeeRates() {
            // given
            PaymentAllocation allocation = createAllocation(1L, 10L, 10000L);
            SettlementPolicy policy = createPolicy(10L, 200, 400);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(allocation));
            when(findSettlementPolicyPort.findActiveBySellerIdIn(List.of(10L)))
                    .thenReturn(Map.of(10L, policy));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService).process(eq(command), eq(10L), anyList(), eq(200), eq(400));
        }

        @Test
        @DisplayName("다중 판매자 중 정책이 있는 판매자와 없는 판매자가 혼재하면 각각 적절한 수수료율을 적용한다")
        void shouldMixPolicyAndDefaultFeeRates() {
            // given
            PaymentAllocation seller10Alloc = createAllocation(1L, 10L, 10000L);
            PaymentAllocation seller20Alloc = createAllocation(2L, 20L, 20000L);
            SettlementPolicy policy = createPolicy(10L, 150, 350);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(seller10Alloc, seller20Alloc));
            when(findSettlementPolicyPort.findActiveBySellerIdIn(anyList()))
                    .thenReturn(Map.of(10L, policy));

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService).process(eq(command), eq(10L), anyList(), eq(150), eq(350));
            verify(processSellerSettlementService).process(eq(command), eq(20L), anyList(), eq(300), eq(500));
        }
    }

    @Nested
    @DisplayName("부분 실패 처리")
    class PartialFailureHandling {

        @BeforeEach
        void setUp() {
            lenient().when(defaultSettlementPolicyProvider.getDefaultPgFeeRate()).thenReturn(300);
            lenient().when(defaultSettlementPolicyProvider.getDefaultPlatformFeeRate()).thenReturn(500);
        }

        @Test
        @DisplayName("한 판매자 정산 실패 시 나머지 판매자 정산은 계속 진행된다")
        void shouldContinueProcessingWhenOneSellerFails() {
            // given
            PaymentAllocation seller10Alloc = createAllocation(1L, 10L, 10000L);
            PaymentAllocation seller20Alloc = createAllocation(2L, 20L, 20000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(seller10Alloc, seller20Alloc));
            when(findSettlementPolicyPort.findActiveBySellerIdIn(anyList()))
                    .thenReturn(Map.of());

            doThrow(new RuntimeException("분개 기록 실패"))
                    .doNothing()
                    .when(processSellerSettlementService).process(any(), anyLong(), anyList(), anyInt(), anyInt());

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService, times(2)).process(eq(command), anyLong(), anyList(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("모든 판매자 정산이 실패해도 예외를 던지지 않는다")
        void shouldNotThrowWhenAllSellersFail() {
            // given
            PaymentAllocation seller10Alloc = createAllocation(1L, 10L, 10000L);

            when(findPaymentAllocationPort.findUnsettledAllocations(2026, 2))
                    .thenReturn(List.of(seller10Alloc));
            when(findSettlementPolicyPort.findActiveBySellerIdIn(anyList()))
                    .thenReturn(Map.of());

            doThrow(new RuntimeException("분개 기록 실패"))
                    .when(processSellerSettlementService).process(any(), anyLong(), anyList(), anyInt(), anyInt());

            ExecuteSettlementCommand command = ExecuteSettlementCommand.builder()
                    .year(2026).month(2).build();

            // when
            executeSettlementService.executeSettlement(command);

            // then
            verify(processSellerSettlementService).process(eq(command), eq(10L), anyList(), anyInt(), anyInt());
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
                    .year(2026).month(2).build();

            // when & then
            assertThatThrownBy(() -> executeSettlementService.executeSettlement(command))
                    .isInstanceOf(NoUnsettledAllocationException.class);

            verifyNoInteractions(processSellerSettlementService);
        }
    }
}
