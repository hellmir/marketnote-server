package com.personal.marketnote.product.port.out.fulfillment;

import com.personal.marketnote.product.port.in.result.fulfillment.GetFulfillmentVendorGoodsResult;

/**
 * 풀필먼트 벤더 상품 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 풀필먼트 벤더 상품 조회 기능을 제공합니다.
 */
public interface GetFulfillmentVendorGoodsPort {
    /**
     * @param godNm 상품명
     * @return 풀필먼트 벤더 상품 조회 결과 {@link GetFulfillmentVendorGoodsResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 상품명으로 풀필먼트 벤더 상품을 조회합니다.
     */
    GetFulfillmentVendorGoodsResult getFulfillmentVendorGoods(String godNm);
}
