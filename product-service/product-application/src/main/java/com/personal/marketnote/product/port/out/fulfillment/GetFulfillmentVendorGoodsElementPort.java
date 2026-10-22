package com.personal.marketnote.product.port.out.fulfillment;

import com.personal.marketnote.product.port.in.result.fulfillment.FulfillmentVendorGoodsElementInfoResult;

import java.util.Optional;

/**
 * 풀필먼트 벤더 상품 요소 단건 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 풀필먼트 벤더 상품 요소 단건 조회 기능을 제공합니다.
 */
public interface GetFulfillmentVendorGoodsElementPort {
    /**
     * @param customerGoodsCode 고객사 상품 코드 (product id 기반)
     * @return 풀필먼트 벤더 상품 요소 정보 {@link FulfillmentVendorGoodsElementInfoResult}
     * @Date 2026-10-18
     * @Author 성효빈
     * @Description 지정된 customerGoodsCode의 ACTIVE 상태 풀필먼트 벤더 상품 요소 정보를 단건 조회합니다.
     */
    Optional<FulfillmentVendorGoodsElementInfoResult> getFulfillmentVendorGoodsElement(String customerGoodsCode);
}
