package com.personal.marketnote.commerce.port.in.usecase.settlement;

import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;

/**
 * 실패한 정산 목록을 조회하는 유스케이스.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface GetFailedSettlementsUseCase {

    /**
     * 실패 상태인 정산 목록을 조회한다.
     *
     * @return 실패한 정산 목록
     */
    GetSettlementsResult getFailedSettlements();
}
