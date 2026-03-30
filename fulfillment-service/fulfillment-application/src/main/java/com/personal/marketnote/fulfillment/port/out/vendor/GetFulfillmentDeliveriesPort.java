package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.delivery.FulfillmentDeliveryQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveriesResult;

/**
 * 파스토 출고 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-11
 * @Description 파스토 출고 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveriesPort {

    /**
     * @param query 파스토 출고 목록 조회 쿼리
     * @return 파스토 출고 목록 조회 결과 {@link GetFulfillmentDeliveriesResult}
     * @Date 2026-02-11
     * @Author 성효빈
     * @Description 파스토 출고 목록을 조회합니다.
     */
    GetFulfillmentDeliveriesResult getDeliveries(FulfillmentDeliveryQuery query);
}
