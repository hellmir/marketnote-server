package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.shop.FulfillmentShopQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentShopsResult;

/**
 * 파스토 출고처 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 파스토 출고처 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentShopsPort {

    /**
     * @param query 파스토 출고처 조회 쿼리
     * @return 파스토 출고처 목록 조회 결과 {@link GetFulfillmentShopsResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 파스토 출고처 목록을 조회합니다.
     */
    GetFulfillmentShopsResult getShops(FulfillmentShopQuery query);
}
