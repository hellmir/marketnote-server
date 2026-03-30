package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.returndelivery.FulfillmentReturnGodDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;

/**
 * 파스토 반품 상품 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-20
 * @Description 파스토 반품 상품 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentReturnGodDetailPort {

    /**
     * @param query 파스토 반품 상품 상세 조회 쿼리
     * @return 파스토 반품 상품 상세 조회 결과 {@link GetFulfillmentReturnGodDetailResult}
     * @Date 2026-02-20
     * @Author 성효빈
     * @Description 파스토 반품 상품 상세 정보를 조회합니다.
     */
    GetFulfillmentReturnGodDetailResult getReturnGodDetail(FulfillmentReturnGodDetailQuery query);
}
