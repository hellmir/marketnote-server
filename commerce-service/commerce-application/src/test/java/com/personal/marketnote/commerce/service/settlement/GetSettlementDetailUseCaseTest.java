package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.*;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementDetailResult;
import com.personal.marketnote.commerce.port.out.settlement.FindPaymentAllocationPort;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSettlementDetailUseCase 테스트")
class GetSettlementDetailUseCaseTest {

    @InjectMocks
    private GetSettlementDetailService getSettlementDetailService;

    @Mock
    private FindSettlementPort findSettlementPort;

    @Mock
    private FindPaymentAllocationPort findPaymentAllocationPort;

    private Settlement createSettlement(Long id) {
        return Settlement.from(SettlementSnapshotState.builder()
                .id(id)
                .sellerId(10L)
                .year(2026)
                .month(2)
                .totalAllocatedAmount(100000L)
                .pgFeeAmount(3000L)
                .platformFeeAmount(5000L)
                .sellerPayoutAmount(92000L)
                .status(SettlementStatus.COMPLETED)
                .version(0L)
                .createdAt(LocalDateTime.of(2026, 2, 16, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 16, 10, 0))
                .build());
    }

    private PaymentAllocation createAllocation(Long id, Long orderId, Long sellerId,
                                               Long allocatedAmount, Long settlementId,
                                               PaymentAllocationTransactionType transactionType,
                                               PaymentAllocationTargetType targetType) {
        return PaymentAllocation.from(PaymentAllocationSnapshotState.builder()
                .id(id)
                .orderId(orderId)
                .sellerId(sellerId)
                .allocatedAmount(allocatedAmount)
                .settlementId(settlementId)
                .transactionType(transactionType)
                .targetType(targetType)
                .idempotencyKey("key-" + id)
                .createdAt(LocalDateTime.of(2026, 2, 15, 14, 30))
                .build());
    }

    @Test
    @DisplayName("정산 ID로 배분 목록을 조회하면 해당 정산에 포함된 배분 내역을 반환한다")
    void shouldReturnAllocationsForSettlement() {
        // given
        Long settlementId = 1L;
        Settlement settlement = createSettlement(settlementId);
        PaymentAllocation allocation1 = createAllocation(1L, 100L, 10L, 50000L, settlementId,
                PaymentAllocationTransactionType.ORDER_REGISTRATION, PaymentAllocationTargetType.ORDER);
        PaymentAllocation allocation2 = createAllocation(2L, 101L, 10L, 50000L, settlementId,
                PaymentAllocationTransactionType.ORDER_REGISTRATION, PaymentAllocationTargetType.ORDER);

        when(findSettlementPort.findById(settlementId)).thenReturn(Optional.of(settlement));
        when(findPaymentAllocationPort.findBySettlementId(settlementId))
                .thenReturn(List.of(allocation1, allocation2));

        // when
        List<GetSettlementDetailResult> results = getSettlementDetailService.getSettlementAllocations(settlementId);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).orderId()).isEqualTo(100L);
        assertThat(results.get(0).allocatedAmount()).isEqualTo(50000L);
        assertThat(results.get(1).orderId()).isEqualTo(101L);
        assertThat(results.get(1).allocatedAmount()).isEqualTo(50000L);
        verify(findSettlementPort).findById(settlementId);
        verify(findPaymentAllocationPort).findBySettlementId(settlementId);
    }

    @Test
    @DisplayName("정산에 배분 내역이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoAllocations() {
        // given
        Long settlementId = 1L;
        Settlement settlement = createSettlement(settlementId);

        when(findSettlementPort.findById(settlementId)).thenReturn(Optional.of(settlement));
        when(findPaymentAllocationPort.findBySettlementId(settlementId)).thenReturn(List.of());

        // when
        List<GetSettlementDetailResult> results = getSettlementDetailService.getSettlementAllocations(settlementId);

        // then
        assertThat(results).isEmpty();
        verify(findSettlementPort).findById(settlementId);
        verify(findPaymentAllocationPort).findBySettlementId(settlementId);
    }

    @Test
    @DisplayName("존재하지 않는 정산 ID로 조회하면 SettlementNotFoundException이 발생한다")
    void shouldThrowWhenSettlementNotFound() {
        // given
        Long settlementId = 999L;
        when(findSettlementPort.findById(settlementId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getSettlementDetailService.getSettlementAllocations(settlementId))
                .isInstanceOf(SettlementNotFoundException.class)
                .hasMessageContaining("999");
        verify(findSettlementPort).findById(settlementId);
        verifyNoInteractions(findPaymentAllocationPort);
    }

    @Test
    @DisplayName("배분 내역의 모든 필드가 정확히 매핑된다")
    void shouldMapAllFieldsCorrectly() {
        // given
        Long settlementId = 1L;
        Settlement settlement = createSettlement(settlementId);
        PaymentAllocation allocation = createAllocation(5L, 200L, 15L, 75000L, settlementId,
                PaymentAllocationTransactionType.ORDER_REGISTRATION, PaymentAllocationTargetType.ORDER);

        when(findSettlementPort.findById(settlementId)).thenReturn(Optional.of(settlement));
        when(findPaymentAllocationPort.findBySettlementId(settlementId)).thenReturn(List.of(allocation));

        // when
        List<GetSettlementDetailResult> results = getSettlementDetailService.getSettlementAllocations(settlementId);

        // then
        GetSettlementDetailResult result = results.get(0);
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.orderId()).isEqualTo(200L);
        assertThat(result.sellerId()).isEqualTo(15L);
        assertThat(result.allocatedAmount()).isEqualTo(75000L);
        assertThat(result.transactionType()).isEqualTo(PaymentAllocationTransactionType.ORDER_REGISTRATION);
        assertThat(result.targetType()).isEqualTo(PaymentAllocationTargetType.ORDER);
        assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2026, 2, 15, 14, 30));
    }

    @Test
    @DisplayName("취소 거래가 포함된 배분 내역을 조회한다")
    void shouldReturnAllocationsWithCancellationType() {
        // given
        Long settlementId = 1L;
        Settlement settlement = createSettlement(settlementId);
        PaymentAllocation orderAllocation = createAllocation(1L, 100L, 10L, 50000L, settlementId,
                PaymentAllocationTransactionType.ORDER_REGISTRATION, PaymentAllocationTargetType.ORDER);
        PaymentAllocation cancelAllocation = createAllocation(2L, 100L, 10L, 50000L, settlementId,
                PaymentAllocationTransactionType.CANCELLATION, PaymentAllocationTargetType.ORDER);

        when(findSettlementPort.findById(settlementId)).thenReturn(Optional.of(settlement));
        when(findPaymentAllocationPort.findBySettlementId(settlementId))
                .thenReturn(List.of(orderAllocation, cancelAllocation));

        // when
        List<GetSettlementDetailResult> results = getSettlementDetailService.getSettlementAllocations(settlementId);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).transactionType()).isEqualTo(PaymentAllocationTransactionType.ORDER_REGISTRATION);
        assertThat(results.get(1).transactionType()).isEqualTo(PaymentAllocationTransactionType.CANCELLATION);
    }
}
