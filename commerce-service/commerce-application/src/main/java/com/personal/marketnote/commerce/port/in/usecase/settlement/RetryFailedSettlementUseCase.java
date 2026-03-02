package com.personal.marketnote.commerce.port.in.usecase.settlement;

/**
 * 실패한 정산을 재시도하는 유스케이스.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface RetryFailedSettlementUseCase {

    /**
     * 실패한 정산을 재시도한다.
     *
     * @param settlementId 재시도할 정산 ID
     * @throws com.personal.marketnote.commerce.exception.SettlementNotFoundException 정산이 존재하지 않는 경우
     * @throws com.personal.marketnote.commerce.domain.settlement.InvalidSettlementStatusTransitionException 정산이 FAILED 상태가 아닌 경우
     */
    void retrySettlement(Long settlementId);
}
