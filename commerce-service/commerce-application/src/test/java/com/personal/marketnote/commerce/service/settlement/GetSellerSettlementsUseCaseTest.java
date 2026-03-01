package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
import com.personal.marketnote.commerce.port.in.command.settlement.GetSellerSettlementsQuery;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementResult;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSellerSettlementsUseCase 테스트")
class GetSellerSettlementsUseCaseTest {

    @InjectMocks
    private GetSellerSettlementsService getSellerSettlementsService;

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

    @Test
    @DisplayName("연도와 월을 지정하면 해당 판매자의 특정 월 정산을 반환한다")
    void shouldReturnSettlementForSpecificMonth() {
        // given
        Long sellerId = 10L;
        Settlement settlement = createSettlement(1L, sellerId, 2026, 2,
                100000L, 3000L, 5000L, 92000L, SettlementStatus.COMPLETED);
        when(findSettlementPort.findBySellerIdAndYearAndMonth(sellerId, 2026, 2))
                .thenReturn(Optional.of(settlement));

        GetSellerSettlementsQuery query = GetSellerSettlementsQuery.of(sellerId, 2026, 2);

        // when
        GetSettlementsResult result = getSellerSettlementsService.getSellerSettlements(query);

        // then
        assertThat(result.settlements()).hasSize(1);
        assertThat(result.settlements().get(0).id()).isEqualTo(1L);
        assertThat(result.settlements().get(0).sellerId()).isEqualTo(sellerId);
        assertThat(result.settlements().get(0).year()).isEqualTo(2026);
        assertThat(result.settlements().get(0).month()).isEqualTo(2);
        verify(findSettlementPort).findBySellerIdAndYearAndMonth(sellerId, 2026, 2);
        verifyNoMoreInteractions(findSettlementPort);
    }

    @Test
    @DisplayName("연도와 월을 지정했지만 정산이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyWhenNoSettlementForMonth() {
        // given
        Long sellerId = 10L;
        when(findSettlementPort.findBySellerIdAndYearAndMonth(sellerId, 2026, 3))
                .thenReturn(Optional.empty());

        GetSellerSettlementsQuery query = GetSellerSettlementsQuery.of(sellerId, 2026, 3);

        // when
        GetSettlementsResult result = getSellerSettlementsService.getSellerSettlements(query);

        // then
        assertThat(result.settlements()).isEmpty();
        verify(findSettlementPort).findBySellerIdAndYearAndMonth(sellerId, 2026, 3);
        verifyNoMoreInteractions(findSettlementPort);
    }

    @Test
    @DisplayName("연도만 지정하면 해당 판매자의 해당 연도 전체 정산을 반환한다")
    void shouldReturnAllSettlementsForYear() {
        // given
        Long sellerId = 10L;
        Settlement jan = createSettlement(1L, sellerId, 2026, 1,
                100000L, 3000L, 5000L, 92000L, SettlementStatus.COMPLETED);
        Settlement feb = createSettlement(2L, sellerId, 2026, 2,
                200000L, 6000L, 10000L, 184000L, SettlementStatus.COMPLETED);
        when(findSettlementPort.findAllBySellerIdAndYear(sellerId, 2026))
                .thenReturn(List.of(jan, feb));

        GetSellerSettlementsQuery query = GetSellerSettlementsQuery.of(sellerId, 2026, null);

        // when
        GetSettlementsResult result = getSellerSettlementsService.getSellerSettlements(query);

        // then
        assertThat(result.settlements()).hasSize(2);
        assertThat(result.settlements().get(0).month()).isEqualTo(1);
        assertThat(result.settlements().get(1).month()).isEqualTo(2);
        verify(findSettlementPort).findAllBySellerIdAndYear(sellerId, 2026);
        verifyNoMoreInteractions(findSettlementPort);
    }

    @Test
    @DisplayName("연도만 지정했지만 정산이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyWhenNoSettlementsForYear() {
        // given
        Long sellerId = 10L;
        when(findSettlementPort.findAllBySellerIdAndYear(sellerId, 2025))
                .thenReturn(List.of());

        GetSellerSettlementsQuery query = GetSellerSettlementsQuery.of(sellerId, 2025, null);

        // when
        GetSettlementsResult result = getSellerSettlementsService.getSellerSettlements(query);

        // then
        assertThat(result.settlements()).isEmpty();
        verify(findSettlementPort).findAllBySellerIdAndYear(sellerId, 2025);
        verifyNoMoreInteractions(findSettlementPort);
    }

    @Test
    @DisplayName("정산 결과의 모든 필드가 정확히 매핑된다")
    void shouldMapAllFieldsCorrectly() {
        // given
        Long sellerId = 15L;
        Settlement settlement = createSettlement(5L, sellerId, 2026, 3,
                150000L, 4500L, 7500L, 138000L, SettlementStatus.COMPLETED);
        when(findSettlementPort.findBySellerIdAndYearAndMonth(sellerId, 2026, 3))
                .thenReturn(Optional.of(settlement));

        GetSellerSettlementsQuery query = GetSellerSettlementsQuery.of(sellerId, 2026, 3);

        // when
        GetSettlementsResult result = getSellerSettlementsService.getSellerSettlements(query);

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
        assertThat(item.createdAt()).isNotNull();
        assertThat(item.modifiedAt()).isNotNull();
    }

    @Test
    @DisplayName("findAllBySellerIdAndYear가 판매자 ID를 정확히 전달한다")
    void shouldPassCorrectSellerIdToPort() {
        // given
        Long sellerId = 99L;
        when(findSettlementPort.findAllBySellerIdAndYear(sellerId, 2026))
                .thenReturn(List.of());

        GetSellerSettlementsQuery query = GetSellerSettlementsQuery.of(sellerId, 2026, null);

        // when
        getSellerSettlementsService.getSellerSettlements(query);

        // then
        verify(findSettlementPort).findAllBySellerIdAndYear(99L, 2026);
    }
}
