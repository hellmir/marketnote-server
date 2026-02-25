package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.goods.FasstoGoodsElementQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoGoodsElementsResult;

/**
 * 파스토 상품 구성 요소 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-30
 * @Description 파스토 상품 구성 요소 조회 기능을 제공합니다.
 */
public interface GetFasstoGoodsElementsPort {

    /**
     * @param query 파스토 상품 구성 요소 조회 쿼리
     * @return 파스토 상품 구성 요소 조회 결과 {@link GetFasstoGoodsElementsResult}
     * @Date 2026-01-30
     * @Author 성효빈
     * @Description 파스토 상품 구성 요소를 조회합니다.
     */
    GetFasstoGoodsElementsResult getGoodsElements(FasstoGoodsElementQuery query);
}
