package com.personal.marketnote.commerce.port.in.usecase.settlement;

/**
 * 취소된 정산을 재실행하는 유스케이스.
 * <p>
 * CANCELLED 상태의 정산을 PENDING으로 리셋한 후 분개를 재기록하고 COMPLETED로 전이한다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface ReExecuteSettlementUseCase {

    /**
     * 취소된 정산을 재실행한다.
     * <p>
     * CANCELLED → PENDING → COMPLETED 상태 전이를 수행하며,
     * 분개를 재기록한다. PaymentAllocation은 이미 연결되어 있으므로 재할당하지 않는다.
     * </p>
     *
     * @param settlementId 재실행할 정산 ID
     * @throws com.personal.marketnote.commerce.exception.SettlementNotFoundException                        정산이 존재하지 않는 경우
     * @throws com.personal.marketnote.commerce.domain.settlement.InvalidSettlementStatusTransitionException 정산이 CANCELLED 상태가 아닌 경우
     */
    void reExecuteSettlement(Long settlementId);
}
