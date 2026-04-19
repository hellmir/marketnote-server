package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicySnapshotState;
import com.personal.marketnote.commerce.exception.SettlementPolicyNotFoundException;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.commerce.port.out.settlement.UpdateSettlementPolicyPort;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("DeleteSettlementPolicyUseCase 테스트")
class DeleteSettlementPolicyUseCaseTest {

    @InjectMocks
    private DeleteSettlementPolicyService deleteSettlementPolicyService;

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
    @DisplayName("정산 정책을 비활성화(소프트 삭제)한다")
    void shouldDeactivatePolicySuccessfully() {
        // given
        SettlementPolicy policy = createPolicy();
        when(findSettlementPolicyPort.findById(1L)).thenReturn(Optional.of(policy));
        when(updateSettlementPolicyPort.update(any(SettlementPolicy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        deleteSettlementPolicyService.deletePolicy(1L);

        // then
        ArgumentCaptor<SettlementPolicy> captor = ArgumentCaptor.forClass(SettlementPolicy.class);
        verify(updateSettlementPolicyPort).update(captor.capture());
        assertThat(captor.getValue().isInactive()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 정책을 삭제하면 예외를 던진다")
    void shouldThrowWhenPolicyNotFound() {
        // given
        when(findSettlementPolicyPort.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deleteSettlementPolicyService.deletePolicy(999L))
                .isInstanceOf(SettlementPolicyNotFoundException.class);

        verify(updateSettlementPolicyPort, never()).update(any());
    }
}
