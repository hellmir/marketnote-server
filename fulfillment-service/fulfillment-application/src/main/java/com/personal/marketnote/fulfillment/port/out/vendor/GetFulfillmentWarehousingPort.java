package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.warehousing.FulfillmentWarehousingQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingResult;

/**
 * 파스토 입고 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 파스토 입고 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentWarehousingPort {

    /**
     * @param query 파스토 입고 조회 쿼리
     * @return 파스토 입고 목록 조회 결과 {@link GetFulfillmentWarehousingResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 파스토 입고 목록을 조회합니다.
     */
    GetFulfillmentWarehousingResult getWarehousing(FulfillmentWarehousingQuery query);
}
