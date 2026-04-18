package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsResult;

/**
 * 파스토 상품 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-29
 * @Description 파스토 상품 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentGoodsPort {

    /**
     * @param command 파스토 상품 등록 커맨드
     * @return 파스토 상품 등록 결과 {@link RegisterFulfillmentGoodsResult}
     * @Date 2026-01-29
     * @Author 성효빈
     * @Description 파스토 상품을 등록합니다.
     */
    RegisterFulfillmentGoodsResult registerGoods(RegisterFulfillmentGoodsCommand command);
}
