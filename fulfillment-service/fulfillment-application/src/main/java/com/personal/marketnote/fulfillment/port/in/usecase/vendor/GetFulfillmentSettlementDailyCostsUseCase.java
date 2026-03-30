package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;

/**
 * 파스토 정산 일별 비용 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-08
 * @Description 파스토 정산 일별 비용 조회 기능을 제공합니다.
 */
public interface GetFulfillmentSettlementDailyCostsUseCase {
    /**
     * @param command 정산 일별 비용 조회 커맨드
     * @return 정산 일별 비용 조회 결과 {@link GetFulfillmentSettlementDailyCostsResult}
     * @Date 2026-02-08
     * @Author 성효빈
     * @Description 파스토 물류비 일별 비용을 조회합니다.
     */
    GetFulfillmentSettlementDailyCostsResult getDailyCosts(GetFulfillmentSettlementDailyCostsCommand command);
}
