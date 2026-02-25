package com.personal.marketnote.product.port.out.fulfillment;

import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsElementsResult;

/**
 * 풀필먼트 벤더 상품 요소 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 풀필먼트 벤더 상품 요소 조회 기능을 제공합니다.
 */
public interface GetFulfillmentVendorGoodsElementsPort {
    /**
     * @return 풀필먼트 벤더 상품 요소 조회 결과 {@link GetFulfillmentVendorGoodsElementsResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 풀필먼트 벤더 상품 등록에 필요한 요소 목록을 조회합니다.
     */
    GetFulfillmentVendorGoodsElementsResult getFulfillmentVendorGoodsElements();
}
