package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.SettlementCycle;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicy;
import com.personal.marketnote.commerce.domain.settlement.SettlementPolicySnapshotState;
import com.personal.marketnote.commerce.exception.SettlementPolicyNotFoundException;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementPolicyResult;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPolicyPort;
import com.personal.marketnote.common.domain.EntityStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSettlementPolicyUseCase 테스트")
class GetSettlementPolicyUseCaseTest {

    @InjectMocks
    private GetSettlementPolicyService getSettlementPolicyService;

    @Mock
    private FindSettlementPolicyPort findSettlementPolicyPort;

    private SettlementPolicy createPolicy(Long id, Long sellerId) {
        return SettlementPolicy.from(SettlementPolicySnapshotState.builder()
                .id(id)
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
    @DisplayName("ID로 정산 정책을 조회한다")
    void shouldGetPolicyById() {
        // given
        SettlementPolicy policy = createPolicy(1L, 10L);
        when(findSettlementPolicyPort.findById(1L)).thenReturn(Optional.of(policy));

        // when
        GetSettlementPolicyResult result = getSettlementPolicyService.getPolicy(1L);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.sellerId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 예외를 던진다")
    void shouldThrowWhenPolicyNotFoundById() {
        // given
        when(findSettlementPolicyPort.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getSettlementPolicyService.getPolicy(999L))
                .isInstanceOf(SettlementPolicyNotFoundException.class);
    }

    @Test
    @DisplayName("판매자 ID로 활성 정산 정책을 조회한다")
    void shouldGetPolicyBySellerId() {
        // given
        SettlementPolicy policy = createPolicy(1L, 10L);
        when(findSettlementPolicyPort.findActiveBySellerId(10L)).thenReturn(Optional.of(policy));

        // when
        GetSettlementPolicyResult result = getSettlementPolicyService.getPolicyBySellerId(10L);

        // then
        assertThat(result.sellerId()).isEqualTo(10L);
        assertThat(result.pgFeeRate()).isEqualTo(300);
    }

    @Test
    @DisplayName("전체 정산 정책을 조회한다")
    void shouldGetAllPolicies() {
        // given
        SettlementPolicy policy1 = createPolicy(1L, 10L);
        SettlementPolicy policy2 = createPolicy(2L, 20L);
        when(findSettlementPolicyPort.findAll()).thenReturn(List.of(policy1, policy2));

        // when
        List<GetSettlementPolicyResult> results = getSettlementPolicyService.getAllPolicies();

        // then
        assertThat(results).hasSize(2);
    }
}
