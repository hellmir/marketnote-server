package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.shop.FulfillmentShopMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;

/**
 * 파스토 출고처 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 파스토 출고처 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentShopPort {

    /**
     * @param request 파스토 출고처 수정 요청 매퍼
     * @return 파스토 출고처 수정 결과 {@link UpdateFulfillmentShopResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 파스토 출고처를 수정합니다.
     */
    UpdateFulfillmentShopResult updateShop(FulfillmentShopMapper request);
}
