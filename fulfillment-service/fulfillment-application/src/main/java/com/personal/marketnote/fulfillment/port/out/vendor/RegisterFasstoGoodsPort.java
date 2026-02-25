package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.goods.FasstoGoodsMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoGoodsResult;

/**
 * 파스토 상품 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-29
 * @Description 파스토 상품 등록 기능을 제공합니다.
 */
public interface RegisterFasstoGoodsPort {

    /**
     * @param request 파스토 상품 등록 요청 매퍼
     * @return 파스토 상품 등록 결과 {@link RegisterFasstoGoodsResult}
     * @Date 2026-01-29
     * @Author 성효빈
     * @Description 파스토 상품을 등록합니다.
     */
    RegisterFasstoGoodsResult registerGoods(FasstoGoodsMapper request);
}
