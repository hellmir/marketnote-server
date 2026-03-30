package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.delivery.FulfillmentDeliveryOutOrdGoodsDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailResult;

/**
 * 파스토 출고 주문 상품 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-13
 * @Description 파스토 출고 주문 상품 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryOutOrdGoodsDetailPort {

    /**
     * @param query 파스토 출고 주문 상품 상세 조회 쿼리
     * @return 파스토 출고 주문 상품 상세 조회 결과 {@link GetFulfillmentDeliveryOutOrdGoodsDetailResult}
     * @Date 2026-02-13
     * @Author 성효빈
     * @Description 파스토 출고 주문 상품 상세 정보를 조회합니다.
     */
    GetFulfillmentDeliveryOutOrdGoodsDetailResult getOutOrdGoodsDetail(FulfillmentDeliveryOutOrdGoodsDetailQuery query);
}
