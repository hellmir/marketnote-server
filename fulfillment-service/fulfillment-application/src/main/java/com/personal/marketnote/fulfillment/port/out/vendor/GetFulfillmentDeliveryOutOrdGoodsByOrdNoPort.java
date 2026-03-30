package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.delivery.FulfillmentDeliveryOutOrdGoodsByOrdNoQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult;

/**
 * 파스토 출고 주문번호별 상품 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 파스토 출고 주문번호별 상품 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryOutOrdGoodsByOrdNoPort {

    /**
     * @param query 파스토 출고 주문번호별 상품 조회 쿼리
     * @return 파스토 출고 주문번호별 상품 조회 결과 {@link GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult}
     * @Date 2026-02-17
     * @Author 성효빈
     * @Description 파스토 출고 주문번호별 상품을 조회합니다.
     */
    GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(FulfillmentDeliveryOutOrdGoodsByOrdNoQuery query);
}
