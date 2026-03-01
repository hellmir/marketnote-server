package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import com.personal.marketnote.commerce.exception.SettlementNotFoundException;
import com.personal.marketnote.commerce.port.in.command.settlement.GetSettlementsQuery;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSettlementUseCase 테스트")
class GetSettlementUseCaseTest {

    @InjectMocks
    private GetSettlementService getSettlementService;

    @Mock
    private FindSettlementPort findSettlementPort;

    private Settlement createSettlement(Long id, Long sellerId, Integer year, Integer month,
                                        Long totalAllocated, Long pgFee, Long platformFee,
                                        Long sellerPayout, SettlementStatus status) {
        return Settlement.from(SettlementSnapshotState.builder()
                .id(id)
                .sellerId(sellerId)
                .year(year)
                .month(month)
                .totalAllocatedAmount(totalAllocated)
                .pgFeeAmount(pgFee)
                .platformFeeAmount(platformFee)
                .sellerPayoutAmount(sellerPayout)
                .status(status)
                .version(0L)
                .createdAt(LocalDateTime.of(2026, 2, 16, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 16, 10, 0))
                .build());
    }

    @Nested
    @DisplayName("정산 단건 조회")
    class GetSettlementTest {

        @Test
        @DisplayName("정산 ID로 COMPLETED 상태의 정산 정보를 조회한다")
        void shouldReturnCompletedSettlementById() {
            // given
            Settlement settlement = createSettlement(1L, 10L, 2026, 2,
                    100000L, 3000L, 5000L, 92000L, SettlementStatus.COMPLETED);
            when(findSettlementPort.findById(1L)).thenReturn(Optional.of(settlement));

            // when
            GetSettlementResult result = getSettlementService.getSettlement(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.sellerId()).isEqualTo(10L);
            assertThat(result.year()).isEqualTo(2026);
            assertThat(result.month()).isEqualTo(2);
            assertThat(result.totalAllocatedAmount()).isEqualTo(100000L);
            assertThat(result.pgFeeAmount()).isEqualTo(3000L);
            assertThat(result.platformFeeAmount()).isEqualTo(5000L);
            assertThat(result.sellerPayoutAmount()).isEqualTo(92000L);
            assertThat(result.status()).isEqualTo(SettlementStatus.COMPLETED);
            assertThat(result.createdAt()).isNotNull();
            assertThat(result.modifiedAt()).isNotNull();
            verify(findSettlementPort).findById(1L);
        }

        @Test
        @DisplayName("PENDING 상태의 정산을 조회한다")
        void shouldReturnPendingSettlement() {
            // given
            Settlement settlement = createSettlement(2L, 20L, 2026, 1,
                    50000L, 1500L, 2500L, 46000L, SettlementStatus.PENDING);
            when(findSettlementPort.findById(2L)).thenReturn(Optional.of(settlement));

            // when
            GetSettlementResult result = getSettlementService.getSettlement(2L);

            // then
            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.status()).isEqualTo(SettlementStatus.PENDING);
            verify(findSettlementPort).findById(2L);
        }

        @Test
        @DisplayName("FAILED 상태의 정산을 조회한다")
        void shouldReturnFailedSettlement() {
            // given
            Settlement settlement = createSettlement(3L, 30L, 2026, 1,
                    80000L, 2400L, 4000L, 73600L, SettlementStatus.FAILED);
            when(findSettlementPort.findById(3L)).thenReturn(Optional.of(settlement));

            // when
            GetSettlementResult result = getSettlementService.getSettlement(3L);

            // then
            assertThat(result.status()).isEqualTo(SettlementStatus.FAILED);
            verify(findSettlementPort).findById(3L);
        }

        @Test
        @DisplayName("존재하지 않는 정산 ID로 조회하면 SettlementNotFoundException이 발생한다")
        void shouldThrowWhenSettlementNotFound() {
            // given
            when(findSettlementPort.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> getSettlementService.getSettlement(999L))
                    .isInstanceOf(SettlementNotFoundException.class)
                    .hasMessageContaining("999");
            verify(findSettlementPort).findById(999L);
        }
    }

    @Nested
    @DisplayName("정산 목록 조회")
    class GetSettlementsTest {

        @Test
        @DisplayName("연/월 기준으로 정산 목록을 조회한다")
        void shouldReturnSettlementsByYearAndMonth() {
            // given
            Settlement settlement1 = createSettlement(1L, 10L, 2026, 2,
                    100000L, 3000L, 5000L, 92000L, SettlementStatus.COMPLETED);
            Settlement settlement2 = createSettlement(2L, 20L, 2026, 2,
                    200000L, 6000L, 10000L, 184000L, SettlementStatus.COMPLETED);

            when(findSettlementPort.findAllByYearAndMonth(2026, 2))
                    .thenReturn(List.of(settlement1, settlement2));

            GetSettlementsQuery query = GetSettlementsQuery.of(2026, 2);

            // when
            GetSettlementsResult result = getSettlementService.getSettlements(query);

            // then
            assertThat(result.settlements()).hasSize(2);
            assertThat(result.settlements().get(0).sellerId()).isEqualTo(10L);
            assertThat(result.settlements().get(1).sellerId()).isEqualTo(20L);
            verify(findSettlementPort).findAllByYearAndMonth(2026, 2);
        }

        @Test
        @DisplayName("해당 연/월에 정산이 없으면 빈 목록을 반환한다")
        void shouldReturnEmptyListWhenNoSettlements() {
            // given
            when(findSettlementPort.findAllByYearAndMonth(2025, 12))
                    .thenReturn(List.of());

            GetSettlementsQuery query = GetSettlementsQuery.of(2025, 12);

            // when
            GetSettlementsResult result = getSettlementService.getSettlements(query);

            // then
            assertThat(result.settlements()).isEmpty();
            verify(findSettlementPort).findAllByYearAndMonth(2025, 12);
        }

        @Test
        @DisplayName("단일 판매자 정산만 있는 경우 목록에 1건이 반환된다")
        void shouldReturnSingleSettlement() {
            // given
            Settlement settlement = createSettlement(1L, 10L, 2026, 1,
                    50000L, 1500L, 2500L, 46000L, SettlementStatus.COMPLETED);

            when(findSettlementPort.findAllByYearAndMonth(2026, 1))
                    .thenReturn(List.of(settlement));

            GetSettlementsQuery query = GetSettlementsQuery.of(2026, 1);

            // when
            GetSettlementsResult result = getSettlementService.getSettlements(query);

            // then
            assertThat(result.settlements()).hasSize(1);
            assertThat(result.settlements().get(0).id()).isEqualTo(1L);
            assertThat(result.settlements().get(0).totalAllocatedAmount()).isEqualTo(50000L);
        }

        @Test
        @DisplayName("정산 목록의 각 항목 필드가 정확히 매핑된다")
        void shouldMapAllFieldsCorrectly() {
            // given
            Settlement settlement = createSettlement(5L, 15L, 2026, 3,
                    150000L, 4500L, 7500L, 138000L, SettlementStatus.COMPLETED);

            when(findSettlementPort.findAllByYearAndMonth(2026, 3))
                    .thenReturn(List.of(settlement));

            GetSettlementsQuery query = GetSettlementsQuery.of(2026, 3);

            // when
            GetSettlementsResult result = getSettlementService.getSettlements(query);

            // then
            GetSettlementResult item = result.settlements().get(0);
            assertThat(item.id()).isEqualTo(5L);
            assertThat(item.sellerId()).isEqualTo(15L);
            assertThat(item.year()).isEqualTo(2026);
            assertThat(item.month()).isEqualTo(3);
            assertThat(item.totalAllocatedAmount()).isEqualTo(150000L);
            assertThat(item.pgFeeAmount()).isEqualTo(4500L);
            assertThat(item.platformFeeAmount()).isEqualTo(7500L);
            assertThat(item.sellerPayoutAmount()).isEqualTo(138000L);
            assertThat(item.status()).isEqualTo(SettlementStatus.COMPLETED);
        }
    }
}
