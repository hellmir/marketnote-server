package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.settlement.FasstoSettlementDailyCostQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoSettlementDailyCostsResult;

/**
 * 파스토 정산 일별 비용 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-08
 * @Description 파스토 정산 일별 비용 조회 기능을 제공합니다.
 */
public interface GetFasstoSettlementDailyCostsPort {

    /**
     * @param query 파스토 정산 일별 비용 조회 쿼리
     * @return 파스토 정산 일별 비용 조회 결과 {@link GetFasstoSettlementDailyCostsResult}
     * @Date 2026-02-08
     * @Author 성효빈
     * @Description 파스토 정산 일별 비용을 조회합니다.
     */
    GetFasstoSettlementDailyCostsResult getDailyCosts(FasstoSettlementDailyCostQuery query);
}
