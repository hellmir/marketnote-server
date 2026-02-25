package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.shop.FasstoShopMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoShopResult;

/**
 * 파스토 출고처 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 파스토 출고처 등록 기능을 제공합니다.
 */
public interface RegisterFasstoShopPort {

    /**
     * @param request 파스토 출고처 등록 요청 매퍼
     * @return 파스토 출고처 등록 결과 {@link RegisterFasstoShopResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 파스토 출고처를 등록합니다.
     */
    RegisterFasstoShopResult registerShop(FasstoShopMapper request);
}
