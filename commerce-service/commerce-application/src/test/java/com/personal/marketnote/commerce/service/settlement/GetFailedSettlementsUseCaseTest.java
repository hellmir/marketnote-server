package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementSnapshotState;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFailedSettlementsUseCase 테스트")
class GetFailedSettlementsUseCaseTest {

    @InjectMocks
    private GetFailedSettlementsService getFailedSettlementsService;

    @Mock
    private FindSettlementPort findSettlementPort;

    @Test
    @DisplayName("실패한 정산이 있으면 목록을 반환한다")
    void shouldReturnFailedSettlements() {
        // given
        Settlement failed1 = Settlement.from(SettlementSnapshotState.builder()
                .id(1L).sellerId(10L).year(2026).month(2)
                .totalAllocatedAmount(100000L).pgFeeAmount(3000L)
                .platformFeeAmount(5000L).sellerPayoutAmount(92000L)
                .status(SettlementStatus.FAILED).version(0L)
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());

        when(findSettlementPort.findAllByStatus(SettlementStatus.FAILED))
                .thenReturn(List.of(failed1));

        // when
        GetSettlementsResult result = getFailedSettlementsService.getFailedSettlements();

        // then
        assertThat(result.settlements()).hasSize(1);
        assertThat(result.settlements().get(0).id()).isEqualTo(1L);
        assertThat(result.settlements().get(0).status()).isEqualTo(SettlementStatus.FAILED);
        verify(findSettlementPort).findAllByStatus(SettlementStatus.FAILED);
    }

    @Test
    @DisplayName("실패한 정산이 없으면 빈 목록을 반환한다")
    void shouldReturnEmptyListWhenNoFailedSettlements() {
        // given
        when(findSettlementPort.findAllByStatus(SettlementStatus.FAILED))
                .thenReturn(List.of());

        // when
        GetSettlementsResult result = getFailedSettlementsService.getFailedSettlements();

        // then
        assertThat(result.settlements()).isEmpty();
    }
}
