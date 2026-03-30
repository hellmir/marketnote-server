package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.settlement.FulfillmentSettlementDailyCostQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;

/**
 * 파스토 정산 일별 비용 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-08
 * @Description 파스토 정산 일별 비용 조회 기능을 제공합니다.
 */
public interface GetFulfillmentSettlementDailyCostsPort {

    /**
     * @param query 파스토 정산 일별 비용 조회 쿼리
     * @return 파스토 정산 일별 비용 조회 결과 {@link GetFulfillmentSettlementDailyCostsResult}
     * @Date 2026-02-08
     * @Author 성효빈
     * @Description 파스토 정산 일별 비용을 조회합니다.
     */
    GetFulfillmentSettlementDailyCostsResult getDailyCosts(FulfillmentSettlementDailyCostQuery query);
}
