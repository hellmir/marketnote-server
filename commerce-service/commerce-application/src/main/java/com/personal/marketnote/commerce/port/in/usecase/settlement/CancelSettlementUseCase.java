package com.personal.marketnote.commerce.port.in.usecase.settlement;

/**
 * 완료된 정산을 취소하는 유스케이스.
 * <p>
 * COMPLETED 상태의 정산을 CANCELLED로 전이하고 역분개를 기록한다.
 * </p>
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface CancelSettlementUseCase {

    /**
     * 완료된 정산을 취소한다.
     * <p>
     * 역분개(reverse journal entry)를 기록하고 CANCELLED 상태로 전이한다.
     * PaymentAllocation의 settlementId는 유지한다.
     * </p>
     *
     * @param settlementId 취소할 정산 ID
     * @throws com.personal.marketnote.commerce.exception.SettlementNotFoundException 정산이 존재하지 않는 경우
     * @throws com.personal.marketnote.commerce.domain.settlement.InvalidSettlementStatusTransitionException 정산이 COMPLETED 상태가 아닌 경우
     */
    void cancelSettlement(Long settlementId);
}
