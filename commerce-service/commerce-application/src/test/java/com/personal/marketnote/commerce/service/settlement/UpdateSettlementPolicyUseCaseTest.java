package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicySnapshotState;
import com.personal.marketnote.commerce.exception.SettlementPolicyNotFoundException;
import com.personal.marketnote.commerce.port.in.command.settlement.UpdateSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPolicyPort;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateSettlementPolicyUseCase 테스트")
class UpdateSettlementPolicyUseCaseTest {

    @InjectMocks
    private UpdateSettlementPolicyService updateSettlementPolicyService;

    @Mock
    private FindSettlementPolicyPort findSettlementPolicyPort;

    @Mock
    private UpdateSettlementPolicyPort updateSettlementPolicyPort;

    private SettlementPolicy createPolicy() {
        return SettlementPolicy.from(SettlementPolicySnapshotState.builder()
                .id(1L)
                .sellerId(10L)
                .pgFeeRate(300)
                .platformFeeRate(500)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(10000L)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }

    @Test
    @DisplayName("정산 정책을 정상 수정한다")
    void shouldUpdatePolicySuccessfully() {
        // given
        SettlementPolicy policy = createPolicy();
        when(findSettlementPolicyPort.findById(1L)).thenReturn(Optional.of(policy));
        when(updateSettlementPolicyPort.update(any(SettlementPolicy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateSettlementPolicyCommand command = UpdateSettlementPolicyCommand.builder()
                .id(1L)
                .pgFeeRate(200)
                .platformFeeRate(400)
                .settlementCycle("BIWEEKLY")
                .minPayoutAmount(5000L)
                .build();

        // when
        GetSettlementPolicyResult result = updateSettlementPolicyService.updatePolicy(command);

        // then
        assertThat(result.pgFeeRate()).isEqualTo(200);
        assertThat(result.platformFeeRate()).isEqualTo(400);
        assertThat(result.settlementCycle()).isEqualTo(SettlementCycle.BIWEEKLY);
        assertThat(result.minPayoutAmount()).isEqualTo(5000L);
        verify(updateSettlementPolicyPort).update(any(SettlementPolicy.class));
    }

    @Test
    @DisplayName("존재하지 않는 정책을 수정하면 예외를 던진다")
    void shouldThrowWhenPolicyNotFound() {
        // given
        when(findSettlementPolicyPort.findById(999L)).thenReturn(Optional.empty());

        UpdateSettlementPolicyCommand command = UpdateSettlementPolicyCommand.builder()
                .id(999L)
                .pgFeeRate(200)
                .platformFeeRate(400)
                .settlementCycle("MONTHLY")
                .minPayoutAmount(5000L)
                .build();

        // when & then
        assertThatThrownBy(() -> updateSettlementPolicyService.updatePolicy(command))
                .isInstanceOf(SettlementPolicyNotFoundException.class);

        verify(updateSettlementPolicyPort, never()).update(any());
    }

    @Test
    @DisplayName("수정 시 수수료율 합계가 100%를 초과하면 예외를 던진다")
    void shouldThrowWhenUpdatedFeeRatesExceed100Percent() {
        // given
        SettlementPolicy policy = createPolicy();
        when(findSettlementPolicyPort.findById(1L)).thenReturn(Optional.of(policy));

        UpdateSettlementPolicyCommand command = UpdateSettlementPolicyCommand.builder()
                .id(1L)
                .pgFeeRate(6000)
                .platformFeeRate(5000)
                .settlementCycle("MONTHLY")
                .minPayoutAmount(0L)
                .build();

        // when & then
        assertThatThrownBy(() -> updateSettlementPolicyService.updatePolicy(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100%");

        verify(updateSettlementPolicyPort, never()).update(any());
    }
}
