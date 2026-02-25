package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.shop.FasstoShopMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFasstoShopResult;

/**
 * 파스토 출고처 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 파스토 출고처 수정 기능을 제공합니다.
 */
public interface UpdateFasstoShopPort {

    /**
     * @param request 파스토 출고처 수정 요청 매퍼
     * @return 파스토 출고처 수정 결과 {@link UpdateFasstoShopResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 파스토 출고처를 수정합니다.
     */
    UpdateFasstoShopResult updateShop(FasstoShopMapper request);
}
