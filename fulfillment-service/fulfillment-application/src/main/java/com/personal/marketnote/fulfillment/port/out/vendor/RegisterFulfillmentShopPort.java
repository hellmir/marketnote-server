package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.shop.FulfillmentShopMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentShopResult;

/**
 * 파스토 출고처 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 파스토 출고처 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentShopPort {

    /**
     * @param request 파스토 출고처 등록 요청 매퍼
     * @return 파스토 출고처 등록 결과 {@link RegisterFulfillmentShopResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 파스토 출고처를 등록합니다.
     */
    RegisterFulfillmentShopResult registerShop(FulfillmentShopMapper request);
}
