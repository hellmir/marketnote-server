package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.goods.FasstoGoodsQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoGoodsResult;

/**
 * 파스토 상품 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-30
 * @Description 파스토 상품 목록 조회 기능을 제공합니다.
 */
public interface GetFasstoGoodsPort {

    /**
     * @param query 파스토 상품 조회 쿼리
     * @return 파스토 상품 목록 조회 결과 {@link GetFasstoGoodsResult}
     * @Date 2026-01-30
     * @Author 성효빈
     * @Description 파스토 상품 목록을 조회합니다.
     */
    GetFasstoGoodsResult getGoods(FasstoGoodsQuery query);
}
