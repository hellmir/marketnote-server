package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryOutOrdGoodsDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryOutOrdGoodsDetailResult;

/**
 * 파스토 출고 주문 상품 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-13
 * @Description 파스토 출고 주문 상품 상세 조회 기능을 제공합니다.
 */
public interface GetFasstoDeliveryOutOrdGoodsDetailPort {

    /**
     * @param query 파스토 출고 주문 상품 상세 조회 쿼리
     * @return 파스토 출고 주문 상품 상세 조회 결과 {@link GetFasstoDeliveryOutOrdGoodsDetailResult}
     * @Date 2026-02-13
     * @Author 성효빈
     * @Description 파스토 출고 주문 상품 상세 정보를 조회합니다.
     */
    GetFasstoDeliveryOutOrdGoodsDetailResult getOutOrdGoodsDetail(FasstoDeliveryOutOrdGoodsDetailQuery query);
}
