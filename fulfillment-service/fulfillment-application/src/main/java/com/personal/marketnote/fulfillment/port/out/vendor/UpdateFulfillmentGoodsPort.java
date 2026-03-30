package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.goods.FulfillmentGoodsMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;

/**
 * 파스토 상품 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-30
 * @Description 파스토 상품 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentGoodsPort {

    /**
     * @param request 파스토 상품 수정 요청 매퍼
     * @return 파스토 상품 수정 결과 {@link UpdateFulfillmentGoodsResult}
     * @Date 2026-01-30
     * @Author 성효빈
     * @Description 파스토 상품을 수정합니다.
     */
    UpdateFulfillmentGoodsResult updateGoods(FulfillmentGoodsMapper request);
}
