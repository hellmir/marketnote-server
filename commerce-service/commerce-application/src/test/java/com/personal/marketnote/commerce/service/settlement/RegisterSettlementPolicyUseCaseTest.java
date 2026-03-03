package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicySnapshotState;
import com.personal.marketnote.commerce.exception.SettlementPolicyAlreadyExistsException;
import com.personal.marketnote.commerce.port.in.command.settlement.RegisterSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.commerce.port.out.settlement.SaveSettlementPolicyPort;
import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
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
@DisplayName("RegisterSettlementPolicyUseCase 테스트")
class RegisterSettlementPolicyUseCaseTest {

    @InjectMocks
    private RegisterSettlementPolicyService registerSettlementPolicyService;

    @Mock
    private FindSettlementPolicyPort findSettlementPolicyPort;

    @Mock
    private SaveSettlementPolicyPort saveSettlementPolicyPort;

    private SettlementPolicy createSavedPolicy(Long sellerId) {
        return SettlementPolicy.from(SettlementPolicySnapshotState.builder()
                .id(1L)
                .sellerId(sellerId)
                .pgFeeRate(300)
                .platformFeeRate(500)
                .settlementCycle(SettlementCycle.MONTHLY)
                .minPayoutAmount(10000L)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 2, 24, 10, 0))
                .build());
    }

    @Test
    @DisplayName("정산 정책을 정상 등록한다")
    void shouldRegisterPolicySuccessfully() {
        // given
        when(findSettlementPolicyPort.findActiveBySellerId(10L)).thenReturn(Optional.empty());
        when(saveSettlementPolicyPort.save(any(SettlementPolicy.class)))
                .thenReturn(createSavedPolicy(10L));

        RegisterSettlementPolicyCommand command = RegisterSettlementPolicyCommand.builder()
                .sellerId(10L)
                .pgFeeRate(300)
                .platformFeeRate(500)
                .settlementCycle("MONTHLY")
                .minPayoutAmount(10000L)
                .build();

        // when
        GetSettlementPolicyResult result = registerSettlementPolicyService.registerPolicy(command);

        // then
        assertThat(result.sellerId()).isEqualTo(10L);
        assertThat(result.pgFeeRate()).isEqualTo(300);
        assertThat(result.platformFeeRate()).isEqualTo(500);
        assertThat(result.settlementCycle()).isEqualTo(SettlementCycle.MONTHLY);

        ArgumentCaptor<SettlementPolicy> captor = ArgumentCaptor.forClass(SettlementPolicy.class);
        verify(saveSettlementPolicyPort).save(captor.capture());
        assertThat(captor.getValue().isActive()).isTrue();
    }

    @Test
    @DisplayName("동일 판매자에 활성 정책이 이미 존재하면 예외를 던진다")
    void shouldThrowWhenPolicyAlreadyExists() {
        // given
        when(findSettlementPolicyPort.findActiveBySellerId(10L))
                .thenReturn(Optional.of(createSavedPolicy(10L)));

        RegisterSettlementPolicyCommand command = RegisterSettlementPolicyCommand.builder()
                .sellerId(10L)
                .pgFeeRate(300)
                .platformFeeRate(500)
                .settlementCycle("MONTHLY")
                .minPayoutAmount(10000L)
                .build();

        // when & then
        assertThatThrownBy(() -> registerSettlementPolicyService.registerPolicy(command))
                .isInstanceOf(SettlementPolicyAlreadyExistsException.class);

        verify(saveSettlementPolicyPort, never()).save(any());
    }

    @Test
    @DisplayName("수수료율 합계가 100%를 초과하면 예외를 던진다")
    void shouldThrowWhenFeeRatesExceed100Percent() {
        // given
        when(findSettlementPolicyPort.findActiveBySellerId(10L)).thenReturn(Optional.empty());

        RegisterSettlementPolicyCommand command = RegisterSettlementPolicyCommand.builder()
                .sellerId(10L)
                .pgFeeRate(6000)
                .platformFeeRate(5000)
                .settlementCycle("MONTHLY")
                .minPayoutAmount(0L)
                .build();

        // when & then
        assertThatThrownBy(() -> registerSettlementPolicyService.registerPolicy(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100%");

        verify(saveSettlementPolicyPort, never()).save(any());
    }

    @Test
    @DisplayName("최소 지급 금액이 음수이면 예외를 던진다")
    void shouldThrowWhenNegativeMinPayoutAmount() {
        // given
        when(findSettlementPolicyPort.findActiveBySellerId(10L)).thenReturn(Optional.empty());

        RegisterSettlementPolicyCommand command = RegisterSettlementPolicyCommand.builder()
                .sellerId(10L)
                .pgFeeRate(300)
                .platformFeeRate(500)
                .settlementCycle("MONTHLY")
                .minPayoutAmount(-1L)
                .build();

        // when & then
        assertThatThrownBy(() -> registerSettlementPolicyService.registerPolicy(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최소 지급 금액");

        verify(saveSettlementPolicyPort, never()).save(any());
    }
}
